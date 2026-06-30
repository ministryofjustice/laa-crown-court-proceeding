package integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.NO_TRIAL_OFFENCES_FOUND;

import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.entity.QueueMessageLogEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl.ProsecutionConcludedImpl;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.scheduler.ProsecutionConcludedScheduler;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.EnableWireMock;

/**
 * This class contains tests where the prosecution concluded message results in a validation error.
 */
@EnableWireMock
@AutoConfigureObservability
@SpringBootTest(classes = {CrownCourtProceedingApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
class ProsecutionConcludedValidationTest extends AbstractProsecutionConcludedTest {

    @Autowired
    ProsecutionConcludedScheduler prosecutionConcludedScheduler;

    @MockitoSpyBean
    ProsecutionConcludedImpl prosecutionConcludedImpl;

    @Test
    void givenAnInvalidMessageWithMissingMaatId_whenListenerIsInvoked_thenMessageIsNotSavedAsADeadLetterMessage() {
        // given - the message is missing the maatId field
        String message = getMessageFromFile("SqsPayloadPleaWithMissingMaatId.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - the message is logged
        thenTheMessageLogTableContainsTheMessage();
        // and - the message is not copied to the dead letter table because the MAAT ID is missing
        thenTheDeadLetterTableIsEmpty();
        // and - no processing was done because of the validation error
        thenTheProsectionConcludedTableIsEmpty();
    }

    @Test
    void givenAnInvalidMessageWithInvalidMaatId_whenListenerIsInvoked_thenMessageIsNotSavedAsADeadLetterMessage() {
        // given - the maatId field is invalid (not a number)
        String message = getMessageFromFile("SqsPayloadPleaWithInvalidMaatId.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - the message is logged
        thenTheMessageLogTableContainsTheMessage();
        // and - the message is not copied to the dead letter table, because the MAAT ID is invalid
        thenTheDeadLetterTableIsEmpty();
        // and - no processing was done because of the validation error
        thenTheProsectionConcludedTableIsEmpty();
    }

    @Test
    void
            givenValidMessageWithMissingMagsOutcome_whenListenerIsInvoked_thenMessageIsReTriedAndSavedAsADeadLetterMessage() {
        // given - the message is linked to a RepOrder (6158011) which has no mags outcome
        String message = getMessageFromFile("SqsAppealsPayloadMissingOutcome.json");

        // when
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - the message is logged
        thenTheMessageLogTableContainsTheMessage();
        // and - the message is copied to the dead letter table - so it appears in the Dropped Prosecution Report
        thenTheDeadLetterTableContainsOnePendingRecordWithReason(
                CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
        // and - it is also sent to retry later - so the CC outcome can be calculated if the Mags outcome is added
        thenTheProsectionConcludedTableContainsOneRecord(6158011);
        // and - the outcome is not sent to MAAT
        thenTheOutcomeIsNotSendToMAAT();
    }

    @Test
    @Disabled("Need to fix LASB-5122 before this test case will pass.")
    void
            givenValidMessageWithMissingMagsOutcomeIsRetried_whenListenerIsInvoked_thenMessageIsReTriedAgainAndNoAdditionalDeadLetterMessageIsCreated() {
        // given - the message has been tried once and failed validation for
        // CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME
        givenValidMessageWithMissingMagsOutcome_whenListenerIsInvoked_thenMessageIsReTriedAndSavedAsADeadLetterMessage();

        // when - the message is retried
        prosecutionConcludedScheduler.process();

        // then - the original message remains in the log table
        thenTheMessageLogTableContainsTheMessage();

        // and - the original message remains in the dead letter table
        thenTheDeadLetterTableContainsOnePendingRecordWithReason(
                CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
        // and - the prosecution concluded record remains in the table in PENDING state
        thenTheProsectionConcludedTableContainsOneRecord(6158011);
        // and - the outcome is not sent to MAAT
        thenTheOutcomeIsNotSendToMAAT();
    }

    private void thenTheOutcomeIsNotSendToMAAT() {
        verify(prosecutionConcludedImpl, never()).execute(any(), any());
    }

    @Test
    void givenAMessageWithUnknownOffenceId_whenListenerIsInvoked_thenMessageIsSavedAsADeadLetterMessage() {
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
        assertThat(deadLetterMessages).hasSize(1);
        assertThat(deadLetterMessages.getFirst().getDeadLetterReason()).isEqualTo(reason);
        assertThat(deadLetterMessages.getFirst().getReportingStatus()).isEqualTo("PENDING");
    }

    private void thenTheMessageLogTableContainsTheMessage() {
        List<QueueMessageLogEntity> all = queueMessageLogRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getMessage()).isNotEmpty();
    }
}
