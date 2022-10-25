package uk.gov.justice.laa.crime.crowncourt.hearing.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class HearingResultedListener {
    @JmsListener(destination = "${cloud-platform.aws.sqs.queue.hearingResulted}", concurrency = "1")
    public void receive(@Payload final String message) {
        log.debug("Hearing Inbound Message : ", message);
    }
}
