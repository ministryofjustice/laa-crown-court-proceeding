package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class CurrentStatusTest {

    @Test
    void givenABlankString_whenGetFromIsInvoked_thenNullIsReturned() {
        assertThat(CurrentStatus.getFrom(null)).isNull();
    }

    @Test
    void givenAValidResultString_whenGetFromIsInvoked_thenCorrectEnumIsReturned() {
        assertThat(CurrentStatus.IN_PROGRESS).isEqualTo(CurrentStatus.getFrom("IN PROGRESS"));
    }

    @Test
    void valueOfCurrentStatusFromString_valueNotFound_throwsException() {
        assertThatThrownBy(
                () -> CurrentStatus.getFrom("MOCK_RESULT_STRING")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenValidInput_ValidateEnumValues() {
        assertThat("IN PROGRESS").isEqualTo(CurrentStatus.IN_PROGRESS.getStatus());
        assertThat("Incomplete").isEqualTo(CurrentStatus.IN_PROGRESS.getDescription());
    }

}