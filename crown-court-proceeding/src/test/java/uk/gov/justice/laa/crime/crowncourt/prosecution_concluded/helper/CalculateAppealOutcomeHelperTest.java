package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculateAppealOutcomeHelperTest {

    private final CalculateAppealOutcomeHelper calculateAppealOutcomeHelper = new CalculateAppealOutcomeHelper();

    @ParameterizedTest
    @CsvSource({"AACD AASA", "AASA AACD"})
    void shouldReturnPartSuccess(String input) {
        assertThat(calculateAppealOutcomeHelper.calculate(input)).isEqualTo("PART SUCCESS");
    }

    @ParameterizedTest
    @CsvSource({"AACA", "AASA"})
    void shouldReturnSuccessful(String input) {
        assertThat(calculateAppealOutcomeHelper.calculate(input)).isEqualTo("SUCCESSFUL");
    }

    @ParameterizedTest
    @CsvSource({"APA", "AW", "AACD", "ASV", "AASD", "ACSD"})
    void shouldReturnUnsuccessful(String input) {
        assertThat(calculateAppealOutcomeHelper.calculate(input)).isEqualTo("UNSUCCESSFUL");
    }
}
