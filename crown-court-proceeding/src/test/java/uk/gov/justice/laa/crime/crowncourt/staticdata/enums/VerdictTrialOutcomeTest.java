package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class VerdictTrialOutcomeTest {

    @Test
    void givenAValidParameter_whenIsConvictedIsInvoked_thenReturnTrue() {
        assertThat(VerdictTrialOutcome.isConvicted(VerdictTrialOutcome.GUILTY.name())).isTrue();
    }

    @Test
    void givenAInvalidParameter_whenIsConvictedIsInvoked_thenReturnFalse() {
        assertThat(VerdictTrialOutcome.isConvicted("AUTREFOIS_CONVICT")).isFalse();
    }

    @Test
    void givenAValidParameter_whenGetTrialOutcomeIsInvoked_thenReturnCorrectValue() {
        assertThat(VerdictTrialOutcome.getTrialOutcome(VerdictTrialOutcome.GUILTY.name()))
                .isEqualTo("CONVICTED");
    }

    @Test
    void givenAInvalidParameter_whenGetTrialOutcomeIsInvoked_thenReturnCorrectValue() {
        assertThat(PleaTrialOutcome.getTrialOutcome("AQUITTED")).isEqualTo("AQUITTED");
    }
}
