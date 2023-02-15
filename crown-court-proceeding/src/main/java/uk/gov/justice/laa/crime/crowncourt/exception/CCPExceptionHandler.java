package uk.gov.justice.laa.crime.crowncourt.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.gov.justice.laa.crime.crowncourt.dto.ErrorDTO;

@RestControllerAdvice
@Slf4j
public class CCPExceptionHandler {

    private static ResponseEntity<ErrorDTO> buildErrorResponse(HttpStatus status, String errorMessage) {
        return new ResponseEntity<>(ErrorDTO.builder().code(status.toString()).message(errorMessage).build(), status);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDTO> handleValidationError(ValidationException ex) {
        log.error("ValidationException: ", ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(APIClientException.class)
    public ResponseEntity<ErrorDTO> handleApiClientError(APIClientException ex) {
        log.error("APIClientException: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(CCPDataException.class)
    public ResponseEntity<ErrorDTO> handleCCPDataException(CCPDataException ex) {
        log.error("CCPDataException: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

}
