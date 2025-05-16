package uk.gov.justice.laa.crime.crowncourt.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.DeadLetterMessageRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeadLetterMessageService {
    private static final String PENDING = "PENDING";
    private final DeadLetterMessageRepository deadLetterMessageRepository;

    public void logDeadLetterMessage(
            String deadLetterReason, ProsecutionConcluded prosecutionConcluded) {
        DeadLetterMessageEntity entity =
                DeadLetterMessageEntity.builder()
                        .deadLetterReason(deadLetterReason)
                        .message(prosecutionConcluded)
                        .receivedTime(LocalDateTime.now())
                        .reportingStatus(PENDING)
                        .build();

        deadLetterMessageRepository.save(entity);
    }
}
