package uk.gov.justice.laa.crime.crowncourt.config;

import org.springframework.context.annotation.Primary;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;
import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * <code>MaatApiOAuth2Client.java</code>
 */
@Configuration
@Slf4j
public class MaatApiOAuth2Client {

    private static final String REGISTERED_ID = "maatapi";
    private final MaatApiConfiguration config;
    private final RetryConfiguration retryConfiguration;

    public MaatApiOAuth2Client(MaatApiConfiguration config, RetryConfiguration retryConfiguration) {
        this.config = config;
        this.retryConfiguration = retryConfiguration;
    }

    @Bean(name = "maatAPIOAuth2WebClient")
    @Primary
    public WebClient webClient(ClientRegistrationRepository clientRegistrations, OAuth2AuthorizedClientRepository authorizedClients) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                        clientRegistrations, authorizedClients
                );

        ConnectionProvider provider =
                ConnectionProvider.builder("custom")
                        .maxConnections(500)
                        .maxIdleTime(Duration.ofSeconds(20))
                        .maxLifeTime(Duration.ofSeconds(60))
                        .pendingAcquireTimeout(Duration.ofSeconds(60))
                        .evictInBackground(Duration.ofSeconds(120))
                        .build();

        oauth.setDefaultClientRegistrationId(REGISTERED_ID);
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
            client.filter(oauth);
        }
        return client.build();
    }

    private ExchangeFilterFunction loggingRequest() {
        return (clientRequest, next) -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }

    private ExchangeFilterFunction loggingResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response status: {}", clientResponse.statusCode());
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
