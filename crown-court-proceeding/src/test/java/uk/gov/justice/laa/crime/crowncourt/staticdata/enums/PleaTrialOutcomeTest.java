package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class PleaTrialOutcomeTest {

    @Test
    void givenAValidParameter_whenIsConvictedIsInvoked_thenReturnTrue() {
        assertThat(PleaTrialOutcome.isConvicted(PleaTrialOutcome.GUILTY_TO_ALTERNATIVE_OFFENCE.name()))
                .isTrue();
    }

    @Test
    void givenAInvalidParameter_whenIsConvictedIsInvoked_thenReturnFalse() {
        assertThat(PleaTrialOutcome.isConvicted(PleaTrialOutcome.AUTREFOIS_CONVICT.name()))
                .isFalse();
    }

    @Test
    void givenAValidParameter_whenGetTrialOutcomeIsInvoked_thenReturnCorrectValue() {
        assertThat(PleaTrialOutcome.getTrialOutcome(PleaTrialOutcome.GUILTY_LESSER_OFFENCE_NAMELY.name()))
                .isEqualTo("CONVICTED");
    }

    @Test
    void givenAInvalidParameter_whenGetTrialOutcomeIsInvoked_thenReturnCorrectValue() {
        assertThat(PleaTrialOutcome.getTrialOutcome("AQUITTED")).isEqualTo("AQUITTED");
    }
}
