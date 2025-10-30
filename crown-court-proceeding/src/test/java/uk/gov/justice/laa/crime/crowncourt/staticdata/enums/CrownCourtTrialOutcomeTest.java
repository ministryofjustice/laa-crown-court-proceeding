package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.AQUITTED;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.CONVICTED;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.PART_CONVICTED;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.isConvicted;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.isTrial;

import uk.gov.justice.laa.crime.exception.ValidationException;

import org.junit.jupiter.api.Test;

class CrownCourtTrialOutcomeTest {

    @Test
    void givenOutcomeIsEmpty_ExceptionThrown() {
        assertThatThrownBy(() -> isConvicted(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void givenOutComeIsConvicted_ReturnsTrue() {
        assertThat(isConvicted(CONVICTED.getValue())).isTrue();
    }

    @Test
    void givenOutComeIsPartConvicted_ReturnsTrue() {
        assertThat(isConvicted(PART_CONVICTED.getValue())).isTrue();
    }

    @Test
    void givenOutComeIsNotConvicted_ReturnsFalse() {
        assertThat(isConvicted(AQUITTED.getValue())).isFalse();
    }

    @Test
    void givenOutcomeIsEmptyForTrial_ExceptionThrown() {
        assertThatThrownBy(() -> isTrial(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void givenOutComeIsForTrial_ReturnsTrue() {
        assertThat(isTrial(CONVICTED.getValue())).isTrue();
    }

    @Test
    void givenOutComeIsNotTrial_ReturnsFalse() {
        assertThat(isTrial("INVALID")).isFalse();
    }
}
