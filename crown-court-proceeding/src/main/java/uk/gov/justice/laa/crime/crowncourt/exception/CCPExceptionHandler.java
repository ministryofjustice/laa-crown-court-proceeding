package uk.gov.justice.laa.crime.crowncourt.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.justice.laa.crime.commons.exception.APIClientException;
import uk.gov.justice.laa.crime.commons.tracing.TraceIdHandler;
import uk.gov.justice.laa.crime.dto.ErrorDTO;
import uk.gov.justice.laa.crime.exception.ValidationException;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class CCPExceptionHandler {

    private final TraceIdHandler traceIdHandler;

    private static ResponseEntity<ErrorDTO> buildErrorResponse(HttpStatus status, String errorMessage, String traceId) {
        return new ResponseEntity<>(ErrorDTO.builder().traceId(traceId).code(status.toString()).message(errorMessage).build(), status);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDTO> handleValidationError(ValidationException ex) {
        log.error("ValidationException: ", ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), traceIdHandler.getTraceId());
    }

    @ExceptionHandler(APIClientException.class)
    public ResponseEntity<ErrorDTO> handleApiClientError(APIClientException ex) {
        log.error("APIClientException: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), traceIdHandler.getTraceId());
    }

    @ExceptionHandler(CCPDataException.class)
    public ResponseEntity<ErrorDTO> handleCCPDataException(CCPDataException ex) {
        log.error("CCPDataException: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), traceIdHandler.getTraceId());
    }

}
