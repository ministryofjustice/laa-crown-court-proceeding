package uk.gov.justice.laa.crime.crowncourt.config;

import io.github.resilience4j.retry.RetryRegistry;
import io.netty.resolver.DefaultAddressResolverGroup;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import uk.gov.justice.laa.crime.crowncourt.client.CourtDataAdaptorApiClient;
import uk.gov.justice.laa.crime.crowncourt.client.EvidenceApiClient;
import uk.gov.justice.laa.crime.crowncourt.filter.Resilience4jRetryFilter;
import uk.gov.justice.laa.crime.crowncourt.client.MaatCourtDataApiClient;
import uk.gov.justice.laa.crime.crowncourt.filter.WebClientFilters;

@Configuration
@AllArgsConstructor
@Slf4j
public class WebClientsConfiguration {
  public static final int MAX_IN_MEMORY_SIZE = 10485760;

  public static final String COURT_DATA_API_WEB_CLIENT_NAME = "maatCourtDataWebClient";
  public static final String EVIDENCE_API_WEB_CLIENT_NAME = "evidenceWebClient";
  public static final String COURT_DATA_ADAPTOR_API_WEB_CLIENT_NAME = "courtDataAdaptorWebClient";

  @Bean
  WebClientCustomizer webClientCustomizer() {
    ConnectionProvider provider =
        ConnectionProvider.builder("custom")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .build();

    return builder -> {
      builder.clientConnector(
          new ReactorClientHttpConnector(
              HttpClient.create(provider)
                  .resolver(DefaultAddressResolverGroup.INSTANCE)
                  .compress(true)
                  .responseTimeout(Duration.ofSeconds(30))
          )
      );
      builder.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
      builder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      builder.codecs(configurer -> configurer
          .defaultCodecs()
          .maxInMemorySize(MAX_IN_MEMORY_SIZE)
      );
    };
  }

  @Bean(COURT_DATA_API_WEB_CLIENT_NAME)
  WebClient maatCourtDataWebClient(WebClient.Builder webClientBuilder,
      ServicesConfiguration servicesConfiguration,
      ClientRegistrationRepository clientRegistrations,
      OAuth2AuthorizedClientRepository authorizedClients,
      RetryRegistry retryRegistry) {

    ServletOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
        new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations,
            authorizedClients);
    oauthFilter.setDefaultClientRegistrationId(
        servicesConfiguration.getMaatApi().getRegistrationId());

    uk.gov.justice.laa.crime.crowncourt.filter.Resilience4jRetryFilter retryFilter =
        new Resilience4jRetryFilter(retryRegistry, COURT_DATA_API_WEB_CLIENT_NAME);

    return webClientBuilder
        .baseUrl(servicesConfiguration.getMaatApi().getAssessmentsDomain())
        .filters(filters -> configureFilters(filters, oauthFilter, retryFilter))
        .build();
  }

  @Bean(EVIDENCE_API_WEB_CLIENT_NAME)
  WebClient evidenceWebClient(WebClient.Builder webClientBuilder,
      ServicesConfiguration servicesConfiguration,
      ClientRegistrationRepository clientRegistrations,
      OAuth2AuthorizedClientRepository authorizedClients,
      RetryRegistry retryRegistry) {

    ServletOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
        new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations,
            authorizedClients);
    oauthFilter.setDefaultClientRegistrationId(
        servicesConfiguration.getEvidence().getRegistrationId());

    Resilience4jRetryFilter retryFilter =
        new Resilience4jRetryFilter(retryRegistry, EVIDENCE_API_WEB_CLIENT_NAME);

    return webClientBuilder
        .baseUrl(servicesConfiguration.getEvidence().getEvidenceDomain())
        .filters(filters -> configureFilters(filters, oauthFilter, retryFilter))
        .build();
  }

  @Bean(COURT_DATA_ADAPTOR_API_WEB_CLIENT_NAME)
  WebClient courtDataAdaptorWebClient(WebClient.Builder webClientBuilder,
      ServicesConfiguration servicesConfiguration,
      ClientRegistrationRepository clientRegistrations,
      OAuth2AuthorizedClientRepository authorizedClients,
      RetryRegistry retryRegistry) {

    ServletOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
        new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations,
            authorizedClients);
    oauthFilter.setDefaultClientRegistrationId(
        servicesConfiguration.getCourtDataAdapter().getRegistrationId());

    Resilience4jRetryFilter retryFilter =
        new Resilience4jRetryFilter(retryRegistry, COURT_DATA_ADAPTOR_API_WEB_CLIENT_NAME);

    return webClientBuilder
        .baseUrl(servicesConfiguration.getCourtDataAdapter().getCourtDataAdapterDomain())
        .filters(filters -> configureFilters(filters, oauthFilter, retryFilter))
        .build();
  }

  @Bean
  MaatCourtDataApiClient maatCourtDataApiClient(
      @Qualifier("maatCourtDataWebClient") WebClient maatCourtDataWebClient) {
    HttpServiceProxyFactory httpServiceProxyFactory =
        HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(maatCourtDataWebClient))
            .build();
    return httpServiceProxyFactory.createClient(MaatCourtDataApiClient.class);
  }

  @Bean
  EvidenceApiClient evidenceApiClient(@Qualifier("evidenceWebClient") WebClient evidenceApiClient) {
    HttpServiceProxyFactory httpServiceProxyFactory =
        HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(evidenceApiClient))
            .build();
    return httpServiceProxyFactory.createClient(EvidenceApiClient.class);
  }

  @Bean
  CourtDataAdaptorApiClient courtDataAdaptorApiClient(
      @Qualifier("courtDataAdaptorWebClient") WebClient courtDataAdaptorApiClient) {
    HttpServiceProxyFactory httpServiceProxyFactory =
        HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(courtDataAdaptorApiClient))
            .build();
    return httpServiceProxyFactory.createClient(CourtDataAdaptorApiClient.class);
  }

  private void configureFilters(List<ExchangeFilterFunction> filters,
      ServletOAuth2AuthorizedClientExchangeFilterFunction oauthFilter,
      ExchangeFilterFunction retryFilter) {
    filters.add(WebClientFilters.logRequestHeaders());
    filters.add(retryFilter);
    filters.add(oauthFilter);
    filters.add(WebClientFilters.errorResponseHandler());
    filters.add(WebClientFilters.handleNotFoundResponse());
    filters.add(WebClientFilters.logResponse());
  }
}
