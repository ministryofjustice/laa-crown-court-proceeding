package uk.gov.justice.laa.crime.crowncourt.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;

class WebClientFiltersTest {

    public static final ClientRequest CLIENT_REQUEST = ClientRequest.create(
                    HttpMethod.GET, URI.create("https://example.com"))
            .build();

    @Test
    void givenRequestWithHeaders_whenLogRequestHeadersFilterApplied_thenResponseIsPassedThrough() {
        // given
        ClientResponse dummyResponse = ClientResponse.create(HttpStatus.OK).build();

        // Use an AtomicReference to capture the request passed along the filter chain.
        AtomicReference<ClientRequest> capturedRequest = new AtomicReference<>();

        // when
        Mono<ClientResponse> result = WebClientFilters.logRequestHeaders().filter(CLIENT_REQUEST, req -> {
            capturedRequest.set(req);
            return Mono.just(dummyResponse);
        });
        ClientResponse response = result.block();

        // then
        assertThat(capturedRequest.get()).isEqualTo(CLIENT_REQUEST);
        // Also verify that the response is passed through unchanged.
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenClientResponse_whenLogResponseFilterApplied_thenResponseIsUnchanged() {
        // given
        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                .header("X-Test", "headerValue")
                .build();

        // when
        Mono<ClientResponse> result = WebClientFilters.logResponse().filter(CLIENT_REQUEST, req -> Mono.just(response));
        ClientResponse filteredResponse = result.block();

        // then
        assertThat(filteredResponse).isNotNull();
        assertThat(filteredResponse.statusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenSuccessResponse_whenErrorResponseHandlerApplied_thenResponseIsPassedThrough() {
        // given
        ClientResponse successResponse = ClientResponse.create(HttpStatus.OK).build();

        // when
        Mono<ClientResponse> result =
                WebClientFilters.errorResponseHandler().filter(CLIENT_REQUEST, req -> Mono.just(successResponse));
        ClientResponse filteredResponse = result.block();

        // then
        assertThat(filteredResponse).isNotNull();
        assertThat(filteredResponse.statusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 500})
    void givenErrorResponse_whenErrorResponseHandlerApplied_thenThrowsWebClientResponseException(int statusCode) {
        // given
        ClientResponse errorResponse =
                ClientResponse.create(HttpStatus.valueOf(statusCode)).build();

        // when
        Mono<ClientResponse> result =
                WebClientFilters.errorResponseHandler().filter(CLIENT_REQUEST, req -> Mono.just(errorResponse));

        // then
        assertThatThrownBy(result::block).isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void givenNotFoundResponse_whenHandleNotFoundResponseApplied_thenReturnsSyntheticResponse() {
        // given
        ClientResponse notFoundResponse = ClientResponse.create(HttpStatus.NOT_FOUND)
                .header("X-Test", "value")
                .build();

        // when
        Mono<ClientResponse> result =
                WebClientFilters.handleNotFoundResponse().filter(CLIENT_REQUEST, req -> Mono.just(notFoundResponse));
        ClientResponse syntheticResponse = result.block();

        // then
        assertThat(syntheticResponse).isNotNull();
        assertThat(syntheticResponse.statusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenNonNotFoundResponse_whenHandleNotFoundResponseApplied_thenResponseIsUnchanged() {
        // given
        ClientResponse errorResponse =
                ClientResponse.create(HttpStatus.BAD_REQUEST).build();

        // when
        Mono<ClientResponse> result =
                WebClientFilters.handleNotFoundResponse().filter(CLIENT_REQUEST, req -> Mono.just(errorResponse));
        ClientResponse response = result.block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
