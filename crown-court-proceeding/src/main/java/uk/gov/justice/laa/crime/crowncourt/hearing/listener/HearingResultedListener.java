package uk.gov.justice.laa.crime.crowncourt.hearing.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(value = "feature.postMvp.enabled", havingValue = "true")
public class HearingResultedListener {
    @JmsListener(destination = "${cloud-platform.aws.sqs.queue.hearingResulted}", concurrency = "1")
    public void receive(@Payload final String message) {
        log.info("Hearing Inbound Message : {}", message);
    }
}
