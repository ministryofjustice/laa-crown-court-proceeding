package uk.gov.justice.laa.crime.crowncourt.exception;

public class RetryableWebClientResponseException extends RuntimeException {

    public RetryableWebClientResponseException() {
        super();
    }

    public RetryableWebClientResponseException(String message) {
        super(message);
    }

    public RetryableWebClientResponseException(Throwable rootCause) {
        super(rootCause);
    }

    public RetryableWebClientResponseException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
}