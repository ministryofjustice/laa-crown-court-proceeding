package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.config;

import io.github.resilience4j.retry.RetryRegistry;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.client.CourtDataAdaptorNonServletApiClient;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.filter.Resilience4jRetryFilter;
import uk.gov.justice.laa.crime.crowncourt.filter.WebClientFilters;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.client.MaatCourtDataNonServletApiClient;

@Configuration
@AllArgsConstructor
@Slf4j
public class NonServletClientConfiguration {

  public static final String COURT_DATA_API_WEB_CLIENT_NAME = "maatApiNonServletClient";
  public static final String COURT_DATA_ADAPTOR_API_WEB_CLIENT_NAME = "cdaApiNonServletClient";

  @Bean
  @Order(2)
  public OAuth2AuthorizedClientManager clientServiceAuthorizedClientManager(
      OAuth2AuthorizedClientService clientService, ClientRegistrationRepository clientRegistrationRepository) {

    OAuth2AuthorizedClientProvider authorizedClientProvider =
        OAuth2AuthorizedClientProviderBuilder.builder()
            .refreshToken()
            .clientCredentials()
            .build();

    AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
        new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, clientService);
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
    return authorizedClientManager;
  }
  
  @Bean(COURT_DATA_API_WEB_CLIENT_NAME)
  WebClient maatApiNonServletClient(WebClient.Builder webClientBuilder,
      ServicesConfiguration servicesConfiguration,
      OAuth2AuthorizedClientManager authorizedClientManager,
      RetryRegistry retryRegistry) {

    ServletOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
        new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    oauthFilter.setDefaultClientRegistrationId(
        servicesConfiguration.getMaatApi().getRegistrationId());

    uk.gov.justice.laa.crime.crowncourt.filter.Resilience4jRetryFilter retryFilter =
        new Resilience4jRetryFilter(retryRegistry, COURT_DATA_API_WEB_CLIENT_NAME);

    return webClientBuilder
        .baseUrl(servicesConfiguration.getMaatApi().getBaseUrl())
        .filters(filters -> configureFilters(filters, oauthFilter, retryFilter))
        .build();
  }
  
  
  @Bean(COURT_DATA_ADAPTOR_API_WEB_CLIENT_NAME)
  WebClient courtDataAdaptorWebClient(WebClient.Builder webClientBuilder,
      ServicesConfiguration servicesConfiguration,
      OAuth2AuthorizedClientManager authorizedClientManager,
      RetryRegistry retryRegistry) {

    ServletOAuth2AuthorizedClientExchangeFilterFunction oauthFilter =
        new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    oauthFilter.setDefaultClientRegistrationId(
        servicesConfiguration.getCourtDataAdapter().getRegistrationId());

    Resilience4jRetryFilter retryFilter =
        new Resilience4jRetryFilter(retryRegistry, COURT_DATA_ADAPTOR_API_WEB_CLIENT_NAME);

    return webClientBuilder
        .baseUrl(servicesConfiguration.getCourtDataAdapter().getBaseUrl())
        .filters(filters -> configureFilters(filters, oauthFilter, retryFilter))
        .build();
  }

  @Bean
  MaatCourtDataNonServletApiClient maatCourtDataNonServletApiClient(
      @Qualifier("maatApiNonServletClient") WebClient maatCourtDataWebClient) {
    HttpServiceProxyFactory httpServiceProxyFactory =
        HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(maatCourtDataWebClient))
            .build();
    return httpServiceProxyFactory.createClient(MaatCourtDataNonServletApiClient.class);
  }
  
  @Bean
  CourtDataAdaptorNonServletApiClient courtDataAdaptorNonServletApiClient(
      @Qualifier("cdaApiNonServletClient") WebClient courtDataAdaptorApiClient) {
    HttpServiceProxyFactory httpServiceProxyFactory =
        HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(courtDataAdaptorApiClient))
            .build();
    return httpServiceProxyFactory.createClient(CourtDataAdaptorNonServletApiClient.class);
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
