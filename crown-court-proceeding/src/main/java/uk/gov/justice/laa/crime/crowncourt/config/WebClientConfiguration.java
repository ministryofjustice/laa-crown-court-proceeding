package uk.gov.justice.laa.crime.crowncourt.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import uk.gov.justice.laa.crime.crowncourt.util.ExchangeFilterUtils;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

/**
 * <code>MaatApiOAuth2Client.java</code>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

    private final ServicesConfiguration configuration;
    private final RetryConfiguration retryConfiguration;


    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("custom")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public WebClientCustomizer webClientCustomizer(ConnectionProvider connectionProvider,
                                                   ClientRegistrationRepository clientRegistrations,
                                                   OAuth2AuthorizedClientRepository authorizedClients) {
        return webClientBuilder -> {

            webClientBuilder.clientConnector(new ReactorClientHttpConnector(
                            HttpClient.create(connectionProvider)
                                    .compress(true)
                                    .responseTimeout(Duration.ofSeconds(30))
                    )
            );

            webClientBuilder.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            webClientBuilder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            webClientBuilder.filters(filters -> {
                filters.add(ExchangeFilterUtils.logResponse());
                filters.add(ExchangeFilterUtils.logRequestHeaders());
                filters.add(ExchangeFilterUtils.retryFilter(retryConfiguration));
                filters.add(ExchangeFilterUtils.handleErrorResponse());

                filters.add(0, new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients));

            });
        };
    }

    public static Consumer<Map<String, Object>> getExchangeFilterWith(String provider) {
        return ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId(provider);
    }


    @Primary
    @Bean(name = "maatAPIOAuth2WebClient")
    public WebClient maatApiWebClient(WebClient.Builder builder) {
        return builder.baseUrl(configuration.getMaatApi().getBaseUrl()).build();
    }

    @Bean(name = "cdaOAuth2WebClient")
    public WebClient cdaWebClient(WebClient.Builder builder) {
        return builder.baseUrl(configuration.getCourtDataAdapter().getBaseUrl()).build();
    }

    @Bean(name = "evidenceOAuth2WebClient")
    public WebClient evidenceWebClient(WebClient.Builder builder) {
        return builder.baseUrl(configuration.getEvidence().getBaseUrl()).build();
    }
}
