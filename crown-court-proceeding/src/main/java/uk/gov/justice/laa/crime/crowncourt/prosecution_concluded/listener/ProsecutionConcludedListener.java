package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;

@Slf4j
@RequiredArgsConstructor
@Component
@XRayEnabled
@ConditionalOnProperty(value = "feature.postMvp.enabled", havingValue = "true")
public class ProsecutionConcludedListener {

    private final Gson gson;

    private final ProsecutionConcludedService prosecutionConcludedService;

    private final QueueMessageLogService queueMessageLogService;

    @SqsListener(value = "${cloud-platform.aws.sqs.queue.prosecutionConcluded}",
            deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void receive(@Payload final String message,
                        final @Headers MessageHeaders headers) {
        try {
            log.debug("message-id {}", headers.get("MessageId"));
            queueMessageLogService.createLog(MessageType.PROSECUTION_CONCLUDED, message);
            ProsecutionConcluded prosecutionConcluded = gson.fromJson(message, ProsecutionConcluded.class);
            prosecutionConcludedService.execute(prosecutionConcluded);
            log.info("CC Outcome is completed for  maat-id {}", prosecutionConcluded.getMaatId());
        } catch (ValidationException exception) {
            log.warn(exception.getMessage());
        }
    }
}