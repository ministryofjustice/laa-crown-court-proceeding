package integration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.NO_TRIAL_OFFENCES_FOUND;

import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.entity.QueueMessageLogEntity;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.EnableWireMock;

/**
 * This class contains tests that where the prosecution concluded message results in a validation error.
 */
@EnableWireMock
@AutoConfigureObservability
@SpringBootTest(classes = {CrownCourtProceedingApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
class ProsecutionConcludedValidationTest extends AbstractProsecutionConcludedTest {

    @Test
    void givenAnInvalidMessageWithMissingMaatId_whenListenerIsInvoked_thenMessageIsSavedAsADeadLetterMessage() {
        // given - the message is missing the maatId field
        String message = getMessageFromFile("SqsPayloadPleaWithMissingMaatId.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - the message is logged
        thenTheMessageLogTableContainsTheMessage();
        // then - the message is not copied to the dead letter table because the MAAT ID is missing
        thenTheDeadLetterTableIsEmpty();
        // then - no processing was done because of the validation error
        thenTheProsectionConcludedTableIsEmpty();
    }

    @Test
    void givenAnInvalidMessageWithInvalidMaatId_whenListenerIsInvoked_thenMessageIsNotSaved() {
        // given - the maatId field is invalid (not a number)
        String message = getMessageFromFile("SqsPayloadPleaWithInvalidMaatId.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - the message is logged
        thenTheMessageLogTableContainsTheMessage();
        // then - no message is not copied to the dead letter table, because the MAAT ID is invalid
        thenTheDeadLetterTableIsEmpty();
        // then - no processing was done because of the validation error
        thenTheProsectionConcludedTableIsEmpty();
    }

    @Test
    void givenValidMessageWithMissingMagsOutcome_whenListenerIsInvoked_thenMessageIsSavedAsADeadLetterMessage() {
        // given - the message is linked to a RepOrder (6518011) which has no mags outcome
        String message = getMessageFromFile("SqsAppealsPayloadMissingOutcome.json");

        // when
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - the message is logged
        thenTheMessageLogTableContainsTheMessage();
        // then - the message is copied to the dead letter table
        thenTheDeadLetterTableContainsOnePendingRecordWithReason(
                CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
        // then - not sure this is right, the code puts the message in the dead letter table but keeps the message in
        // the prosecution concluded table
        thenTheProsectionConcludedTableContainsOneRecord(6158011);
    }

    @Test
    void givenAMessageWithUnknownOffenceId_whenListenerIsInvoked_thenMessageIsMovedToDeadLetterTable() {
        // given - the offenceId in the message does not match any offence for the case
        String message = getMessageFromFile("SqsPayloadWithUnknownOffenceId.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - the message is logged
        thenTheMessageLogTableContainsTheMessage();
        // then - the message is copied to the dead letter table
        thenTheDeadLetterTableContainsOnePendingRecordWithReason(NO_TRIAL_OFFENCES_FOUND);
        // then - no processing was done because of the validation error
        thenTheProsectionConcludedTableIsEmpty();
    }

    private void thenTheProsectionConcludedTableIsEmpty() {
        List<ProsecutionConcludedEntity> list = prosecutionConcludedRepository.findAll();
        assertThat(list).isEmpty();
    }

    private void thenTheProsectionConcludedTableContainsOneRecord(int maatId) {
        List<ProsecutionConcludedEntity> list = prosecutionConcludedRepository.findAll();
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getMaatId()).isEqualTo(maatId);
        assertThat(list.getFirst().getStatus()).isEqualTo("PENDING");
    }

    private void thenTheDeadLetterTableIsEmpty() {
        List<DeadLetterMessageEntity> deadLetterMessages = deadLetterMessageRepository.findAll();
        assertThat(deadLetterMessages).isEmpty();
    }

    private void thenTheDeadLetterTableContainsOnePendingRecordWithReason(String reason) {
        List<DeadLetterMessageEntity> deadLetterMessages = deadLetterMessageRepository.findAll();
        deadLetterMessages.forEach(System.out::println);
        assertThat(deadLetterMessages).hasSize(1);
        assertThat(deadLetterMessages.getFirst().getDeadLetterReason()).isEqualTo(reason);
        assertThat(deadLetterMessages.getFirst().getReportingStatus()).isEqualTo("PENDING");
    }

    private void thenTheMessageLogTableIsEmpty() {
        List<QueueMessageLogEntity> all = queueMessageLogRepository.findAll();
        assertThat(all).isEmpty();
    }

    private void thenTheMessageLogTableContainsTheMessage() {
        List<QueueMessageLogEntity> all = queueMessageLogRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getMessage()).isNotEmpty();
    }
}
