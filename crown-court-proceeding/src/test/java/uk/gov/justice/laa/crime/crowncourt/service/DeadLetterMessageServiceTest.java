package uk.gov.justice.laa.crime.crowncourt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.repository.DeadLetterMessageRepository;

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
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .maatId(123456)
                .build();

        deadLetterMessageService.logDeadLetterMessage(errorReason, prosecutionConcluded);

        verify(deadLetterMessageRepository, times(1)).save(deadLetterMessageCaptor.capture());
        DeadLetterMessageEntity deadLetterMessageEntity = deadLetterMessageCaptor.getValue();

        assertThat(deadLetterMessageEntity.getDeadLetterReason()).isEqualTo(errorReason);
        assertThat(deadLetterMessageEntity.getMessage()).isEqualTo(prosecutionConcluded);
    }
}
