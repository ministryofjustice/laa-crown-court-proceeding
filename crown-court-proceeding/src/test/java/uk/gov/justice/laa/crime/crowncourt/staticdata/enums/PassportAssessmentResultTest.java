package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PassportAssessmentResultTest {

    @Test
    void givenABlankString_whenGetFromIsInvoked_thenNullIsReturned() {
        assertThat(PassportAssessmentResult.getFrom(null)).isNull();
    }

    @Test
    void givenAValidResultString_whenGetFromIsInvoked_thenCorrectEnumIsReturned() {
        assertThat(PassportAssessmentResult.FAIL).isEqualTo(PassportAssessmentResult.getFrom("FAIL"));
    }

    @Test
    void valueOfCurrentStatusFromString_valueNotFound_throwsException() {
        assertThatThrownBy(
                () -> PassportAssessmentResult.getFrom("MOCK_RESULT_STRING")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenValidInput_ValidateEnumValues() {
        assertThat("TEMP").isEqualTo(PassportAssessmentResult.TEMP.getResult());
        assertThat("Temporary Pass").isEqualTo(PassportAssessmentResult.TEMP.getReason());
    }

}