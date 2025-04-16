package uk.gov.justice.laa.crime.crowncourt.filter;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;

import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class Resilience4jRetryFilterTest {

  @InjectSoftAssertions
  private SoftAssertions softly;

  @Mock
  private ExchangeFunction exchangeFunction;

  private static final int NUM_RETRIES = 3;
  private static RetryRegistry retryRegistry;
  public static final String DEFAULT_CONFIG_NAME = "default";
  private static final URI DEFAULT_URL = URI.create("https://example.com");

  @BeforeEach
  void setupConfiguration() {
    RetryConfig retryConfig = RetryConfig.custom()
        .maxAttempts(NUM_RETRIES)
        .retryExceptions(WebClientRequestException.class,
            WebClientResponseException.BadGateway.class,
            WebClientResponseException.TooManyRequests.class,
            WebClientResponseException.NotFound.class
        )
        .failAfterMaxAttempts(true)
        .build();
    retryRegistry = RetryRegistry.of(retryConfig);
  }

  @Test
  void givenRetriesExhausted_whenRetryFilterIsInvoked_thenFinalExceptionIsThrown() {
    ClientRequest request = ClientRequest.create(HttpMethod.GET, DEFAULT_URL).build();
    LinkedList<RuntimeException> errors = new LinkedList<>(
        Arrays.asList(getWebClientResponseException(HttpStatus.NOT_FOUND),
            getWebClientResponseException(HttpStatus.TOO_MANY_REQUESTS),
            getWebClientResponseException(HttpStatus.BAD_GATEWAY)
        )
    );
    Mono<ClientResponse> errorMono = getClientResponseMono(errors);

    when(exchangeFunction.exchange(request))
        .thenReturn(errorMono);

    Mono<ClientResponse> response =
        new Resilience4jRetryFilter(retryRegistry, DEFAULT_CONFIG_NAME)
            .filter(request, exchangeFunction);

    softly.assertThatThrownBy(
            response::block
        ).isInstanceOf(WebClientResponseException.BadGateway.class)
        .hasMessageContaining("502 Bad Gateway");

    verifyCorrectNumberOfCalls(NUM_RETRIES, DEFAULT_CONFIG_NAME);
    softly.assertAll();
  }

  @Test
  void givenSuccessfulResponseWithoutRetry_whenRetryFilterIsInvoked_thenOkResponseIsReturned() {
    ClientRequest request = ClientRequest.create(HttpMethod.GET, DEFAULT_URL).build();
    Mono<ClientResponse> responseMono = Mono.just(ClientResponse.create(HttpStatus.OK).build());

    when(exchangeFunction.exchange(request))
        .thenReturn(responseMono);

    Mono<ClientResponse> clientResponse =
        new Resilience4jRetryFilter(retryRegistry, DEFAULT_CONFIG_NAME)
            .filter(request, exchangeFunction);

    ClientResponse response = clientResponse.block();

    verifyCorrectNumberOfCalls(1, DEFAULT_CONFIG_NAME);
    softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
    softly.assertAll();
  }

  @Test
  void givenSuccessfulResponseFollowingRetry_whenRetryFilterIsInvoked_thenOkResponseIsReturned() {
    ClientRequest request = ClientRequest.create(HttpMethod.GET, DEFAULT_URL).build();
    LinkedList<RuntimeException> errors = new LinkedList<>(
        Arrays.asList(getWebClientResponseException(HttpStatus.TOO_MANY_REQUESTS),
            getWebClientResponseException(HttpStatus.BAD_GATEWAY)
        )
    );

    Mono<ClientResponse> responseMono =
        getClientResponseMono(errors);

    when(exchangeFunction.exchange(request))
        .thenReturn(responseMono);

    Mono<ClientResponse> clientResponse =
        new Resilience4jRetryFilter(retryRegistry, DEFAULT_CONFIG_NAME)
            .filter(request, exchangeFunction);

    ClientResponse response = clientResponse.block();

    verifyCorrectNumberOfCalls(NUM_RETRIES, DEFAULT_CONFIG_NAME);
    softly.assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
    softly.assertAll();
  }

  private void verifyCorrectNumberOfCalls(int numRetries, String configName) {
    Retry retry = retryRegistry.retry(configName);
    long numberOfTotalCalls = retry.getMetrics().getNumberOfTotalCalls();
    softly.assertThat(numberOfTotalCalls).isEqualTo(numRetries);
  }

  @Test
  void givenUnRetryableException_whenRetryFilterIsInvoked_thenNoRetriesAreAttempted() {
    ClientRequest request = ClientRequest.create(HttpMethod.GET, DEFAULT_URL).build();

    when(exchangeFunction.exchange(request))
        .thenReturn(Mono.error(getWebClientResponseException(HttpStatus.UNAUTHORIZED)));

    Mono<ClientResponse> response =
        new Resilience4jRetryFilter(retryRegistry, DEFAULT_CONFIG_NAME)
            .filter(request, exchangeFunction);

    softly.assertThatThrownBy(
            response::block
        ).isInstanceOf(WebClientResponseException.class)
        .hasMessageContaining("401 Unauthorized");

    verifyCorrectNumberOfCalls(1, DEFAULT_CONFIG_NAME);
    softly.assertAll();
  }

  @Test
  void givenOverrideConfiguration_whenRetryFilterIsInvoked_thenCorrectConfigurationIsApplied() {
    RetryConfig retryConfig = RetryConfig.custom()
        .maxAttempts(2)
        .retryExceptions(WebClientResponseException.Conflict.class)
        .failAfterMaxAttempts(true)
        .build();
    retryRegistry.retry("override", retryConfig);

    ClientRequest request = ClientRequest.create(HttpMethod.GET, DEFAULT_URL).build();

    LinkedList<RuntimeException> errors = new LinkedList<>(
        Arrays.asList(getWebClientResponseException(HttpStatus.CONFLICT),
            getWebClientResponseException(HttpStatus.CONFLICT),
            getWebClientResponseException(HttpStatus.CONFLICT)
        )
    );

    Mono<ClientResponse> errorMono = getClientResponseMono(errors);

    when(exchangeFunction.exchange(request))
        .thenReturn(errorMono);

    Mono<ClientResponse> response =
        new Resilience4jRetryFilter(retryRegistry, "override")
            .filter(request, exchangeFunction);

    softly.assertThatThrownBy(
            response::block
        ).isInstanceOf(WebClientResponseException.Conflict.class)
        .hasMessageContaining("409 Conflict");

    verifyCorrectNumberOfCalls(2, "override");
    softly.assertAll();
  }

  @Test
  void givenMissingOverrideConfiguration_whenRetryFilterIsInvoked_thenDefaultConfigurationIsApplied() {
    ClientRequest request = ClientRequest.create(HttpMethod.GET, DEFAULT_URL).build();

    LinkedList<RuntimeException> errors = new LinkedList<>(
        Arrays.asList(getWebClientResponseException(HttpStatus.BAD_GATEWAY),
            getWebClientResponseException(HttpStatus.CONFLICT),
            getWebClientResponseException(HttpStatus.TOO_MANY_REQUESTS)
        )
    );

    Mono<ClientResponse> errorMono = getClientResponseMono(errors);

    when(exchangeFunction.exchange(request))
        .thenReturn(errorMono);

    Mono<ClientResponse> response =
        new Resilience4jRetryFilter(retryRegistry, "override")
            .filter(request, exchangeFunction);

    softly.assertThatThrownBy(
            response::block
        ).isInstanceOf(WebClientResponseException.Conflict.class)
        .hasMessageContaining("409 Conflict");

    verifyCorrectNumberOfCalls(2, DEFAULT_CONFIG_NAME);
    softly.assertAll();
  }

  private static WebClientResponseException getWebClientResponseException(HttpStatus status) {
    return WebClientResponseException.create(
        status.value(),
        status.getReasonPhrase(),
        new HttpHeaders(),
        new byte[0],
        null
    );
  }

  private static Mono<ClientResponse> getClientResponseMono(LinkedList<RuntimeException> errors) {
    return Mono.defer(
        () -> {
          if (errors.isEmpty()) {
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
          }
          Throwable error = errors.pop();
          log.info("Simulating failure: {}", error.getClass().getSimpleName());
          return Mono.error(error);
        }
    );
  }
}
