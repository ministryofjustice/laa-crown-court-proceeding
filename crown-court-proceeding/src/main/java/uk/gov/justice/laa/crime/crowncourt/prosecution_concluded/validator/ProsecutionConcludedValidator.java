package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ApplicationConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@RequiredArgsConstructor
public class ProsecutionConcludedValidator {

    private final CrownCourtCodeHelper crownCourtCodeHelper;

    public static final String PAYLOAD_IS_NOT_AVAILABLE_OR_NULL = "Payload is not available or null.";
    public static final String OU_CODE_IS_MISSING = "OU Code is missing.";
    public static final String OU_CODE_LOOKUP_FAILED = "OU Code lookup failed.";
    public static final String MAAT_ID_FORMAT_INCORRECT = "MAAT ID has incorrect format.";
    public static final String CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME =
        "Cannot have Crown Court outcome without Mags Court outcome";

    public void validateRequestObject(ProsecutionConcluded prosecutionConcluded) {
        if (prosecutionConcluded == null
                || prosecutionConcluded.getOffenceSummary() == null
                || prosecutionConcluded.getOffenceSummary().isEmpty()
                || prosecutionConcluded.getMaatId() == null)
            throw new ValidationException(PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    public void validateOuCode(String ouCode) {
        if (isBlank(ouCode)) {
            throw new ValidationException(OU_CODE_IS_MISSING);
        }

        if (!crownCourtCodeHelper.isValidCode(ouCode)) {
            throw new ValidationException(OU_CODE_LOOKUP_FAILED);
        }
    }

    public void validateMaatId(String message) {
        try {
            JsonObject msgObject = JsonParser.parseString(message).getAsJsonObject();
            JsonElement maatIdElement = msgObject.get("maatId");

            // MAAT ID is checked for a null value in validateRequestObject, after the message has
            // been deserialised from JSON. This method is called before that happens, so the check
            // here is that it is of the correct type.
            if (maatIdElement != null && !StringUtils.isEmpty(maatIdElement.getAsString())) {
                msgObject.get("maatId").getAsInt();
            }
        } catch (NumberFormatException ex) {
            throw new ValidationException(MAAT_ID_FORMAT_INCORRECT);
        }
    }

    public void validateMagsCourtOutcomeExists(String magsCourtOutcome) {
        if (StringUtils.isEmpty(magsCourtOutcome)) {
            throw new ValidationException(CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
        }
    }
    
    public void validateIsAppealMissing(String caseType) {
        if (CaseType.APPEAL_CC.getCaseType().equals(caseType)) {
            throw new ValidationException(CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
        }
    }
    
    public void validateApplicationResultCode(ApplicationConcluded applicationConcluded) {
        if (applicationConcluded != null) {
            String applicationResult = applicationConcluded.getApplicationResultCode();
            if (applicationResult == null) {
                throw new ValidationException("Application Result Code is missing.");
            } else {
                List<String> resultCodes = List.of(new String[]{"APA", "AW", "AACD", "ASV",
                        "AACA", "AASD", "AASA", "ACSD"});
                if (!(resultCodes.contains(applicationResult) || (applicationResult.contains("AACD") && applicationResult.contains("AASA")))) {
                    throw new ValidationException("Application Result Code is invalid.");
                }
            }
        }
    }
}

