package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProsecutionConcludedListener {
    @JmsListener(destination = "${PROSECUTION_CONCLUDED_QUEUE}", concurrency = "1")
    public void receive(@Payload final String message) {
        log.debug("Prosecution Inbound Message : ", message);
    }
}