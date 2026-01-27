package uk.gov.justice.laa.crime.crowncourt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME;

import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.repository.DeadLetterMessageRepository;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeadLetterMessageServiceTest {
    @InjectMocks
    private DeadLetterMessageService deadLetterMessageService;

    @Spy
    private DeadLetterMessageRepository deadLetterMessageRepository;

    @Captor
    private ArgumentCaptor<DeadLetterMessageEntity> deadLetterMessageCaptor;

    @Test
    void givenErrorReasonAndQueueMessage_whenLogDeadLetterMessageIsInvoked_thenDeadLetterMessageIsCreated() {
        String errorReason = ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL;
        ProsecutionConcluded prosecutionConcluded =
                ProsecutionConcluded.builder().maatId(123456).build();
        String reportingStatus = "PENDING";

        deadLetterMessageService.logDeadLetterMessage(errorReason, prosecutionConcluded);

        verify(deadLetterMessageRepository, times(1)).save(deadLetterMessageCaptor.capture());
        DeadLetterMessageEntity deadLetterMessageEntity = deadLetterMessageCaptor.getValue();

        assertThat(deadLetterMessageEntity.getDeadLetterReason()).isEqualTo(errorReason);
        assertThat(deadLetterMessageEntity.getMessage()).isEqualTo(prosecutionConcluded);
        assertThat(deadLetterMessageEntity.getReportingStatus()).isEqualTo(reportingStatus);
    }

    @Test
    void existsInDeadLetterQueueReturnsTrueWhenEntriesFoundForMaatId() {
        Integer maatId = 987654;
        DeadLetterMessageEntity entity = DeadLetterMessageEntity.builder()
                .message(uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded.builder()
                        .maatId(maatId)
                        .build())
                .build();
        when(deadLetterMessageRepository.findByMaatIdAndDeadLetterReasonNot(
                        maatId, CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME))
                .thenReturn(List.of(entity));

        boolean result = deadLetterMessageService.hasNoDeadLetterMessageForMaatId(
                maatId, CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);

        assertThat(result).isFalse();
        verify(deadLetterMessageRepository, times(1))
                .findByMaatIdAndDeadLetterReasonNot(maatId, CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
    }

    @Test
    void existsInDeadLetterQueueReturnsFalseWhenNoEntriesFoundForMaatId() {
        Integer maatId = 111111;
        when(deadLetterMessageRepository.findByMaatIdAndDeadLetterReasonNot(
                        maatId, CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME))
                .thenReturn(Collections.emptyList());

        boolean result = deadLetterMessageService.hasNoDeadLetterMessageForMaatId(
                maatId, CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);

        assertThat(result).isTrue();
        verify(deadLetterMessageRepository, times(1))
                .findByMaatIdAndDeadLetterReasonNot(maatId, CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
    }
}
