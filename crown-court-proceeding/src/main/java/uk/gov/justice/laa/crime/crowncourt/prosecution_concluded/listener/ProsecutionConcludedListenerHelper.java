package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;
import uk.gov.justice.laa.crime.exception.ValidationException;

import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * This class is responsible for processing the message from the SQS queue.
 * It deliberately doesn't use any SQS-specific classes, so the message processing can be tested
 * easily by the integration tests.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProsecutionConcludedListenerHelper {

    private final Gson gson;
    private final QueueMessageLogService queueMessageLogService;
    private final ProsecutionConcludedService prosecutionConcludedService;
    private final ProsecutionConcludedValidator prosecutionConcludedValidator;
    private final DeadLetterMessageService deadLetterMessageService;

    public void receive(final String message, final MessageHeaders headers) {
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

            if (!ProsecutionConcludedValidator.MAAT_ID_FORMAT_INCORRECT.equalsIgnoreCase(exception.getMessage())) {
                deadLetterMessageService.logDeadLetterMessage(exception.getMessage(), prosecutionConcluded);
            }
        }
    }
}
