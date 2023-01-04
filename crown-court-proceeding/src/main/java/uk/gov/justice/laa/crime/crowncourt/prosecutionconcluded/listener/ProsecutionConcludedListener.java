package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "feature.postMvp.enabled", havingValue = "true")
public class ProsecutionConcludedListener {
    @JmsListener(destination = "${cloud-platform.aws.sqs.queue.prosecutionConcluded}", concurrency = "1")
    public void receive(@Payload final String message) {
        log.info("Prosecution Inbound Message : {} ", message);
    }
}