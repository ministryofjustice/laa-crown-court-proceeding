package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum CrownCourtCaseType {
    INDICTABLE("INDICTABLE"),
    SUMMARY_ONLY("SUMMARY ONLY"),
    CC_ALREADY("CC ALREADY"),
    EITHER_WAY("EITHER WAY"),
    APPEAL_CC("APPEAL CC");

    private final String value;

    public static boolean caseTypeForTrial(final String caseType) {

        return Stream.of(INDICTABLE, EITHER_WAY, CC_ALREADY)
                .anyMatch(csType -> csType.getValue().equalsIgnoreCase(notEmpty(caseType)));
    }

    public static boolean caseTypeForAppeal(final String caseType) {
        return APPEAL_CC.getValue().equalsIgnoreCase(notEmpty(caseType));
    }

    private static String notEmpty(String caseType) {
        return Optional.ofNullable(caseType).orElseThrow(() -> new ValidationException("Case type can't be empty."));
    }
}
