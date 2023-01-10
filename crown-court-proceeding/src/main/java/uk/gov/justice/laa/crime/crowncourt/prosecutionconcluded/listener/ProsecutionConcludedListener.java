package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "feature.postMvp.enabled", havingValue = "true")
public class ProsecutionConcludedListener {
    private final QueueMessageLogService queueMessageLogService;

    @JmsListener(destination = "${cloud-platform.aws.sqs.queue.prosecutionConcluded}", concurrency = "1")
    public void receive(@Payload final String message) {
        log.info("Prosecution Inbound Message : {} ", message);
        queueMessageLogService.createLog(MessageType.PROSECUTION_CONCLUDED, message);
    }
}