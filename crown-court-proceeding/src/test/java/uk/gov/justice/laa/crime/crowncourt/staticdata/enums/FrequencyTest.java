package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FrequencyTest {

    @Test
    void givenValidWeeklyInput_ValidateEnumValues() {
        assertThat("WEEKLY").isEqualTo(Frequency.WEEKLY.getCode());
        assertThat("Weekly").isEqualTo(Frequency.WEEKLY.getDescription());
        assertThat(52).isEqualTo(Frequency.WEEKLY.getAnnualWeighting());
        assertThat("WEEKLY").isEqualTo(Frequency.WEEKLY.getValue());
    }
}