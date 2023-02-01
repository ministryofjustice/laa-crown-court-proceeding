package uk.gov.justice.laa.crime.crowncourt.config;

import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;

import java.time.Duration;

/**
 * <code>CourtDataAdapterOAuth2ClientConfig</code>
 */
@Configuration
@Slf4j
public class CourtDataAdapterOAuth2ClientConfig {

    private static final String REGISTERED_ID = "cda";

    private final MaatApiConfiguration config;

    private final RetryConfiguration retryConfiguration;

    public CourtDataAdapterOAuth2ClientConfig (MaatApiConfiguration config, RetryConfiguration retryConfiguration) {
        this.config = config;
        this.retryConfiguration = retryConfiguration;
    }

    /**
     * @param tokenUri
     * @param clientId
     * @param clientSecret
     * @return
     */
    @Bean
    ClientRegistrationRepository getRegistration(
            @Value("${spring.security.oauth2.client.provider.cda.token-uri}") String tokenUri,
            @Value("${spring.security.oauth2.client.registration.cda.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.cda.client-secret}") String clientSecret
    ) {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId(REGISTERED_ID)
                .tokenUri(tokenUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        return new InMemoryClientRegistrationRepository(registration);
    }


    /**
     * @param clientRegistrationRepository
     * @return
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository) {

        // grant_type = client_credentials flow.
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        // Machine to machine service.
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository));
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }


    /**
     * @param authorizedClientManager
     * @return
     */
    @Bean(name = "cdaOAuth2WebClient")
    public WebClient webClient(@Value("${cda.url}") String baseUrl, OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId(REGISTERED_ID);
        ConnectionProvider provider =
                ConnectionProvider.builder("custom")
                        .maxConnections(500)
                        .maxIdleTime(Duration.ofSeconds(20))
                        .maxLifeTime(Duration.ofSeconds(60))
                        .pendingAcquireTimeout(Duration.ofSeconds(60))
                        .evictInBackground(Duration.ofSeconds(120))
                        .build();

        oauth2Client.setDefaultClientRegistrationId(REGISTERED_ID);
        WebClient.Builder client = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .filter(retryFilter())
                .filter(loggingRequest())
                .filter(errorResponse())
                .filter(loggingResponse())
                .clientConnector(new ReactorClientHttpConnector(
                                HttpClient.create(provider)
                                        .compress(true)
                                        .responseTimeout(Duration.ofSeconds(30))
                        )
                )
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (config.isOAuthEnabled()) {
            client.filter(oauth2Client);
        }
        return client.build();
    }

    /**
     *
     * @return
     */
    private ExchangeFilterFunction loggingRequest() {
        return (clientRequest, next) -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }

    /**
     *
     * @return
     */
    private ExchangeFilterFunction loggingResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response status: {}",clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    private ExchangeFilterFunction errorResponse() {
        return ExchangeFilterFunctions.statusError(
                HttpStatus::isError, r -> {
                    String errorMessage =
                            String.format("Received error %s due to %s", r.statusCode().value(), r.statusCode().getReasonPhrase());
                    if (r.statusCode().is5xxServerError()) {
                        return new HttpServerErrorException(
                                r.statusCode(),
                                errorMessage
                        );
                    }
                    if (r.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return WebClientResponseException.create(r.rawStatusCode(), r.statusCode().getReasonPhrase(), null, null, null);
                    }
                    return new APIClientException(errorMessage);
                });
    }

    private ExchangeFilterFunction retryFilter() {
        return (request, next) ->
                next.exchange(request)
                        .retryWhen(
                                Retry.backoff(
                                                retryConfiguration.getMaxRetries(),
                                                Duration.ofSeconds(
                                                        retryConfiguration.getMinBackOffPeriod()
                                                )
                                        )
                                        .jitter(retryConfiguration.getJitterValue())
                                        .filter(
                                                throwable ->
                                                        throwable instanceof HttpServerErrorException ||
                                                                (throwable instanceof WebClientRequestException && throwable.getCause() instanceof TimeoutException)
                                        ).onRetryExhaustedThrow(
                                                (retryBackoffSpec, retrySignal) ->
                                                        new APIClientException(
                                                                String.format(
                                                                        "Call to Court Data API failed. Retries exhausted: %d/%d.",
                                                                        retryConfiguration.getMaxRetries(),
                                                                        retryConfiguration.getMaxRetries()
                                                                ), retrySignal.failure()
                                                        )
                                        ).doBeforeRetry(
                                                doBeforeRetry -> log.warn(
                                                        String.format("Call to Court Data API failed, retrying: %d/%d",
                                                                doBeforeRetry.totalRetries(), retryConfiguration.getMaxRetries()
                                                        )
                                                )
                                        )
                        );
    }

}
