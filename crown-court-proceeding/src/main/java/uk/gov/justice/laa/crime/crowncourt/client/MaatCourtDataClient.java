package uk.gov.justice.laa.crime.crowncourt.client;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaatCourtDataClient {

    private final WebClient webClient;

    public <T> T getApiResponseViaGET(Class<T> responseClass, String url, Map<String, String> headers, Object... urlVariables) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(url)
                        .build(urlVariables))
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .retrieve()
                .bodyToMono(responseClass)
                .onErrorResume(WebClientResponseException.NotFound.class, notFound -> Mono.empty())
                .onErrorMap(this::handleError)
                .doOnError(Sentry::captureException)
                .block();
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

        return webClient
                .method(requestMethod)
                .uri(url)
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
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
        return new APIClientException("Call to Court Data API failed, invalid response.", error);
    }
}
