package uk.gov.justice.laa.crime.crowncourt.filter;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Slf4j
@UtilityClass
public class WebClientFilters {

  public static ExchangeFilterFunction logResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
      if ((clientResponse.statusCode().is4xxClientError() && clientResponse.statusCode() != HttpStatus.NOT_FOUND) 
          || clientResponse.statusCode().is5xxServerError()) {
        log.error("❌  Response status: {}", clientResponse.statusCode());
      } else if (clientResponse.statusCode().is2xxSuccessful()) {
        log.info("✅ Response status: {}", clientResponse.statusCode());
      }
      return Mono.just(clientResponse);
    });
  }

  public static ExchangeFilterFunction logRequestHeaders() {
    return (clientRequest, next) -> {
      log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
      clientRequest.headers()
          .forEach((name, values) -> {
            if (!name.equals(HttpHeaders.AUTHORIZATION)) {
              values.forEach(value -> log.info("{}={}", name, value));
            }
          });
      return next.exchange(clientRequest);
    };
  }

  public static ExchangeFilterFunction handleNotFoundResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(response -> {
      if (response.statusCode() == HttpStatus.NOT_FOUND) {
        // Create a synthetic successful response (200 OK) with an empty body.
        return response.bodyToMono(Void.class)
            .then(Mono.just(ClientResponse.create(HttpStatus.OK).build()));
      }
      return Mono.just(response);
    });
  }

  public static ExchangeFilterFunction errorResponseHandler() {
    return ExchangeFilterFunction.ofResponseProcessor(response -> {
      if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
        return response.createException().flatMap(Mono::error);
      }

      return Mono.just(response);
    });
  }
}