package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.APPEAL_CC;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.CC_ALREADY;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.EITHER_WAY;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.INDICTABLE;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.caseTypeForAppeal;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.caseTypeForTrial;

import uk.gov.justice.laa.crime.exception.ValidationException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CrownCourtCaseTypeTest {

    @Test
    void givenCaseTypeForTrialIsEmpty_ExceptionThrown() {

        Assertions.assertThrows(ValidationException.class, () -> {
            caseTypeForTrial(null);
        });
    }

    @Test
    void givenCaseTypeForTrialIsIndictable_ReturnsTrue() {

        assertAll("CaseTypeForTrial", () -> assertTrue(caseTypeForTrial(INDICTABLE.getValue())));
    }

    @Test
    void givenCaseTypeForTrialIsEitherWayOnly_ReturnsTrue() {

        assertAll("CaseTypeForTrial", () -> assertTrue(caseTypeForTrial(EITHER_WAY.getValue())));
    }

    @Test
    void givenCaseTypeForTrialIsCCAlready_ReturnsTrue() {

        assertAll("CaseTypeForTrial", () -> assertTrue(caseTypeForTrial(CC_ALREADY.getValue())));
    }

    @Test
    void givenCaseTypeForTrialNotValid_ReturnsFalse() {

        assertAll("CaseTypeForTrial", () -> assertFalse(caseTypeForTrial(APPEAL_CC.getValue())));
    }

    @Test
    void givenCaseTypeForAppealIsEmpty_ExceptionThrown() {

        Assertions.assertThrows(ValidationException.class, () -> {
            caseTypeForAppeal(null);
        });
    }

    @Test
    void givenCaseTypeForAppealIsAppealCC_ReturnsTrue() {

        assertAll("CaseTypeForAppeal", () -> assertTrue(caseTypeForAppeal(APPEAL_CC.getValue())));
    }

    @Test
    void givenCaseTypeNotForAppeal_ReturnsFalse() {

        assertAll("CaseTypeForAppeal", () -> assertFalse(caseTypeForAppeal(EITHER_WAY.getValue())));
    }
}
