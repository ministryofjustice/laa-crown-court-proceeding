package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@XRayEnabled
public class ProsecutionConcludedValidator {

    public void validateRequestObject(ProsecutionConcluded prosecutionConcluded) {
        if (prosecutionConcluded == null
                || prosecutionConcluded.getOffenceSummary() == null
                || prosecutionConcluded.getOffenceSummary().isEmpty()
                || prosecutionConcluded.getMaatId() == null)
            throw new ValidationException("Payload is not available or null. ");
    }

    public Optional<Void> validateOuCode(String ouCode) {

        if (!isBlank(ouCode)) {
            return Optional.empty();
        } else {
            throw new ValidationException("OU Code is missing.");
        }
    }
}
