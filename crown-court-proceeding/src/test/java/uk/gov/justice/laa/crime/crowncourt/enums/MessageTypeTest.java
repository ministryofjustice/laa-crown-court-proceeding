package uk.gov.justice.laa.crime.crowncourt.enums;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MessageTypeTest {

    @Test
    void givenValidInput_ValidateEnumValues() {
        assertThat("LAA_STATUS_UPDATE").isEqualTo(MessageType.LAA_STATUS_UPDATE.name());
        assertThat("PROSECUTION_CONCLUDED").isEqualTo(MessageType.PROSECUTION_CONCLUDED.name());
    }

}