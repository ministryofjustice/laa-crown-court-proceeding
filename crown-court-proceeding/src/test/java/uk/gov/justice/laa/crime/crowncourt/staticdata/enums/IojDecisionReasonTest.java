package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class IojDecisionReasonTest {

    @Test
    void givenValidResultString_whenGetFromIsInvoked_thenCorrectEnumIsReturned() {
        assertThat(IojDecisionReason.getFrom("NOTUNDPROC")).isEqualTo(IojDecisionReason.NOTUNDPROC);
    }

    @Test
    void givenBlankString_whenGetFromIsInvoked_thenNullIsReturned() {
        assertThat(IojDecisionReason.getFrom(null)).isNull();
    }

    @Test
    void givenInvalidResultString_whenGetFromIsInvoked_thenExceptionIsThrown() {
        assertThatThrownBy(
                () -> IojDecisionReason.getFrom("MOCK_RESULT_STRING")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testEnumAttributes() {
        assertThat("OTHER").isEqualTo(IojDecisionReason.OTHER.getCode());
        assertThat(10).isEqualTo(IojDecisionReason.OTHER.getSequence());
        assertThat("Other").isEqualTo(IojDecisionReason.OTHER.getDescription());
    }
}
