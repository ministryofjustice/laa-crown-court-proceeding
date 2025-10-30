package uk.gov.justice.laa.crime.crowncourt.filter;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

@Slf4j
@UtilityClass
public class WebClientFilters {
    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().is2xxSuccessful() || response.statusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                log.info("✅ Response status: {}", response.statusCode());
            } else if (response.statusCode().is4xxClientError()
                    || response.statusCode().is5xxServerError()) {
                log.error("❌  Response status: {}", response.statusCode());
            }
            return Mono.just(response);
        });
    }

    public static ExchangeFilterFunction logRequestHeaders() {
        return (request, next) -> {
            log.info("Request: {} {}", request.method(), request.url());
            request.headers().forEach((name, values) -> {
                if (!name.equals(HttpHeaders.AUTHORIZATION)) {
                    values.forEach(value -> log.debug("{}={}", name, value));
                }
            });
            return next.exchange(request);
        };
    }

    public static ExchangeFilterFunction handleNotFoundResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                return response.bodyToMono(Void.class)
                        .then(Mono.just(ClientResponse.create(HttpStatus.OK).build()));
            }
            return Mono.just(response);
        });
    }

    public static ExchangeFilterFunction errorResponseHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().is4xxClientError()
                    || response.statusCode().is5xxServerError()) {
                return response.createException().flatMap(Mono::error);
            }
            return Mono.just(response);
        });
    }
}
