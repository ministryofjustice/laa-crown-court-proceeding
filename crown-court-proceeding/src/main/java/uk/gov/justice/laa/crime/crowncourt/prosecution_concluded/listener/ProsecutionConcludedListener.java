package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "feature.prosecution-concluded-listener.enabled", havingValue = "true")
public class ProsecutionConcludedListener {

    private final ProsecutionConcludedListenerHelper prosecutionConcludedListenerHelper;

    @SqsListener(value = "${cloud-platform.aws.sqs.queue.prosecutionConcluded}")
    public void receive(@Payload final String message, final @Headers MessageHeaders headers) {
        prosecutionConcludedListenerHelper.receive(message, headers);
    }
}
