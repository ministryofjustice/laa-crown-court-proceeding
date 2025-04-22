package uk.gov.justice.laa.crime.crowncourt.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.crime.dto.ErrorDTO;
import uk.gov.justice.laa.crime.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.tracing.TraceIdHandler;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class CCPExceptionHandler {

    private final TraceIdHandler traceIdHandler;
    private final ObjectMapper mapper;

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorDTO> onRuntimeException(WebClientResponseException exception) {
        String errorMessage;
        try {
            ErrorDTO errorDTO = mapper.readValue(exception.getResponseBodyAsString(), ErrorDTO.class);
            errorMessage = errorDTO.getMessage();
        } catch (Exception ex) {
            log.warn("Unable to read the ErrorDTO from WebClientResponseException", ex);
            errorMessage = exception.getMessage();
        }
        return buildErrorResponse(exception.getStatusCode(), errorMessage, traceIdHandler.getTraceId());
    }

    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorDTO> onRuntimeException(WebClientRequestException exception) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), traceIdHandler.getTraceId());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDTO> handleValidationError(ValidationException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), traceIdHandler.getTraceId());
    }

    @ExceptionHandler(CCPDataException.class)
    public ResponseEntity<ErrorDTO> handleCCPDataException(CCPDataException ex) {
        log.error("CCPDataException: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), traceIdHandler.getTraceId());
    }

    private static ResponseEntity<ErrorDTO> buildErrorResponse(HttpStatusCode status, String message, String traceId) {
        log.error("Exception Occurred. Status - {}, Detail - {}, TraceId - {}", status, message, traceId);
        return new ResponseEntity<>(ErrorDTO.builder().traceId(traceId).code(status.toString()).message(message).build(), status);
    }
}
