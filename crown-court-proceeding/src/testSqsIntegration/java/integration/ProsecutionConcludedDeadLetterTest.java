package integration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL;

import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.EnableWireMock;

/**
 * This class contains tests that result in a ProsecutionConcluded message being sent to the
 * dead letter table DEAD_LETTER_MESSAGE because it cannot be processed.
 */
@EnableWireMock
@AutoConfigureObservability
@SpringBootTest(classes = {CrownCourtProceedingApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
class ProsecutionConcludedDeadLetterTest extends AbstractProsecutionConcludedTest {

    @Test
    void givenValidMessageWithMissingOutcome_whenListenerIsInvoked_thenMessageIsSavedAsADeadLetterMessage() {
        // given - the message has a missing outcome
        String message = getMessageFromFile("SqsAppealsPayloadMissingOutcome.json");

        // when
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - the message is sent to the dead letter table
        List<DeadLetterMessageEntity> deadLetterMessages = deadLetterMessageRepository.findAll();
        assertThat(deadLetterMessages).hasSize(1);
        assertThat(deadLetterMessages.getFirst().getMessage().getMaatId()).isEqualTo(6158011);
        assertThat(deadLetterMessages.getFirst().getDeadLetterReason())
                .isEqualTo(CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
    }

    @Test
    void givenAnInvalidMessageWithMissingMaatId_whenListenerIsInvoked_thenMessageIsSavedAsADeadLetterMessage() {
        // given - the message is missing the maatId field
        String message = getMessageFromFile("SqsPayloadPleaWithMissingMaatId.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - the message is moved to the dead letter table
        List<DeadLetterMessageEntity> deadLetterMessages = deadLetterMessageRepository.findAll();
        assertThat(deadLetterMessages).hasSize(1);
        assertThat(deadLetterMessages.getFirst().getDeadLetterReason()).isEqualTo(PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void givenAnInvalidMessageWithInvalidMaatId_whenListenerIsInvoked_thenMessageIsNotSaved() {
        // given - the maatId field is invalid (not a number)
        String message = getMessageFromFile("SqsPayloadPleaWithInvalidMaatId.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - no message is saved to the dead letter table, because the MAAT ID is invalid
        List<DeadLetterMessageEntity> deadLetterMessages = deadLetterMessageRepository.findAll();
        assertThat(deadLetterMessages).isEmpty();
    }
}
