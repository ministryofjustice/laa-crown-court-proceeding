package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.CONVICTED;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.PART_CONVICTED;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.isConvicted;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.isTrial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.crime.exception.ValidationException;

class CrownCourtTrialOutcomeTest {

    @Test
    void givenOutcomeIsEmpty_ExceptionThrown() {

        Assertions.assertThrows(ValidationException.class, () -> {
            isConvicted(null);
        });
    }

    @Test
    void givenOutComeIsConvicted_ReturnsTrue() {

        assertAll("TrialOutcome",
                () -> assertTrue(isConvicted(CONVICTED.getValue())));
    }

    @Test
    void givenOutComeIsPartConvicted_ReturnsTrue() {

        assertAll("TrialOutcome",
                () -> assertTrue(isConvicted(PART_CONVICTED.getValue())));
    }

    @Test
    void givenOutComeIsNotConvicted_ReturnsFalse() {

        assertAll("TrialOutcome",
                () -> assertFalse(isConvicted("ACQUITTED")));
    }

    @Test
    void givenOutcomeIsEmptyForTrial_ExceptionThrown() {

        Assertions.assertThrows(ValidationException.class, () -> {
            isTrial(null);
        });
    }

    @Test
    void givenOutComeIsForTrial_ReturnsTrue() {

        assertAll("TrialOutcome",
                () -> assertTrue(isTrial(CONVICTED.getValue())));
    }


    @Test
    void givenOutComeIsNotTrial_ReturnsFalse() {

        assertAll("TrialOutcome",
                () -> assertFalse(isTrial("INVALID")));
    }
}
