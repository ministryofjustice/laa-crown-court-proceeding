package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator;

import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ProsecutionConcludedValidator {

    public static final String PAYLOAD_IS_NOT_AVAILABLE_OR_NULL = "Payload is not available or null.";
    public static final String OU_CODE_IS_MISSING = "OU Code is missing.";

    public void validateRequestObject(ProsecutionConcluded prosecutionConcluded) {
        if (prosecutionConcluded == null
                || prosecutionConcluded.getOffenceSummary() == null
                || prosecutionConcluded.getOffenceSummary().isEmpty()
                || prosecutionConcluded.getMaatId() == null)
            throw new ValidationException(PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    public Optional<Void> validateOuCode(String ouCode) {

        if (!isBlank(ouCode)) {
            return Optional.empty();
        } else {
            throw new ValidationException(OU_CODE_IS_MISSING);
        }
    }
}
