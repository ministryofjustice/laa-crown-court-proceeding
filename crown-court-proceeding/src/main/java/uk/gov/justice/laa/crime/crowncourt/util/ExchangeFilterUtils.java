package uk.gov.justice.laa.crime.crowncourt.util;

import io.netty.handler.timeout.TimeoutException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import uk.gov.justice.laa.crime.crowncourt.config.RetryConfiguration;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;

import java.time.Duration;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeFilterUtils {

    public static ExchangeFilterFunction logRequestHeaders() {
        return (clientRequest, next) -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers()
                    .forEach((name, values) -> {
                        if (!name.equals("Authorization")) {
                            values.forEach(value -> log.info("{}={}", name, value));
                        }
                    });
            return next.exchange(clientRequest);
        };
    }


    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("Response status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    public static ExchangeFilterFunction handleErrorResponse() {
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

    public static ExchangeFilterFunction retryFilter(RetryConfiguration retryConfiguration) {
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
                                                                        "Call to service failed. Retries exhausted: %d/%d.",
                                                                        retryConfiguration.getMaxRetries(),
                                                                        retryConfiguration.getMaxRetries()
                                                                ), retrySignal.failure()
                                                        )
                                        ).doBeforeRetry(
                                                doBeforeRetry -> log.warn(
                                                        String.format("Call to service failed, retrying: %d/%d",
                                                                doBeforeRetry.totalRetries(), retryConfiguration.getMaxRetries()
                                                        )
                                                )
                                        )
                        );
    }
}
