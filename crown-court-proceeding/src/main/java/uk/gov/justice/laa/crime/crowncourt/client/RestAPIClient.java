package uk.gov.justice.laa.crime.crowncourt.client;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.crime.crowncourt.config.WebClientConfiguration;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class RestAPIClient {

    protected abstract WebClient getWebClient();

    protected abstract String getRegistrationId();

    public <T> T getApiResponseViaGET(Class<T> responseClass,
                                      String url, Map<String, String> headers,
                                      MultiValueMap<String, String> queryParams,
                                      Object... urlVariables) {
        return getWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder.path(url)
                        .queryParams(queryParams)
                        .build(urlVariables))
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .attributes(WebClientConfiguration.getExchangeFilterWith(getRegistrationId()))
                .retrieve()
                .bodyToMono(responseClass)
                .onErrorResume(WebClientResponseException.NotFound.class, notFound -> Mono.empty())
                .onErrorMap(this::handleError)
                .doOnError(Sentry::captureException)
                .block();
    }

    public <T> T getApiResponseViaGET(Class<T> responseClass, String url, Map<String, String> headers, Object... urlVariables) {
        return getApiResponseViaGET(responseClass, url, headers, null, urlVariables);
    }

    public <T> T getApiResponseViaGET(Class<T> responseClass, String url, Object... urlVariables) {
        return getApiResponseViaGET(responseClass, url, null, null, urlVariables);
    }

    public <T, R> R getApiResponseViaPOST(T requestBody, Class<R> responseClass, String url, Map<String, String> headers) {
        return getApiResponse(requestBody, responseClass, url, headers, HttpMethod.POST);
    }

    public <T, R> R getApiResponseViaPUT(T requestBody, Class<R> responseClass, String url, Map<String, String> headers) {
        return getApiResponse(requestBody, responseClass, url, headers, HttpMethod.PUT);
    }

    <T, R> R getApiResponse(T requestBody,
                            Class<R> responseClass,
                            String url, Map<String, String> headers,
                            HttpMethod requestMethod) {

        return getWebClient()
                .method(requestMethod)
                .uri(url)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .attributes(WebClientConfiguration.getExchangeFilterWith(getRegistrationId()))
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(responseClass)
                .onErrorResume(WebClientResponseException.NotFound.class, notFound -> Mono.empty())
                .onErrorMap(this::handleError)
                .doOnError(Sentry::captureException)
                .block();
    }

    private Throwable handleError(Throwable error) {
        if (error instanceof APIClientException) {
            return error;
        }
        String serviceName = getRegistrationId().toUpperCase();
        return new APIClientException(String.format("Call to service %s failed.", serviceName), error);
    }

    public <R> R getGraphQLApiResponse(Class<R> responseClass,
                                       String url,
                                       Map<String, Object> graphQLBody) {
        return getWebClient()
                .post()
                .uri(url)
                .bodyValue(graphQLBody)
                .retrieve()
                .bodyToMono(responseClass)
                .onErrorMap(this::handleError)
                .doOnError(Sentry::captureException)
                .block();
    }
}
