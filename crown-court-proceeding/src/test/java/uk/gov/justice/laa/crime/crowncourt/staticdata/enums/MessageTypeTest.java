package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

class MessageTypeTest {

    @Test
    void givenValidInput_ValidateEnumValues() {
        assertThat("LAA_STATUS_UPDATE").isEqualTo(MessageType.LAA_STATUS_UPDATE.name());
        assertThat("PROSECUTION_CONCLUDED").isEqualTo(MessageType.PROSECUTION_CONCLUDED.name());
    }
}
