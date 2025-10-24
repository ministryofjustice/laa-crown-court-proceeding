package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;
import uk.gov.justice.laa.crime.exception.ValidationException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "feature.prosecution-concluded-listener.enabled", havingValue = "true")
public class ProsecutionConcludedListener {

    private final Gson gson;
    private final QueueMessageLogService queueMessageLogService;
    private final ProsecutionConcludedService prosecutionConcludedService;
    private final ProsecutionConcludedValidator prosecutionConcludedValidator;
    private final DeadLetterMessageService deadLetterMessageService;

    @SqsListener(value = "${cloud-platform.aws.sqs.queue.prosecutionConcluded}")
    public void receive(@Payload final String message, final @Headers MessageHeaders headers) {
        ProsecutionConcluded prosecutionConcluded = null;

        try {
            log.debug("message-id {}", headers.get("MessageId"));

            prosecutionConcludedValidator.validateMaatId(message);

            queueMessageLogService.createLog(MessageType.PROSECUTION_CONCLUDED, message);
            prosecutionConcluded = gson.fromJson(message, ProsecutionConcluded.class);
            prosecutionConcludedService.execute(prosecutionConcluded);
            log.info("CC Outcome is completed for  maat-id {}", prosecutionConcluded.getMaatId());
        } catch (ValidationException exception) {
            log.warn(exception.getMessage());

            if (!exception.getMessage().equalsIgnoreCase(ProsecutionConcludedValidator.MAAT_ID_FORMAT_INCORRECT)) {
                deadLetterMessageService.logDeadLetterMessage(exception.getMessage(), prosecutionConcluded);
            }
        }
    }
}
