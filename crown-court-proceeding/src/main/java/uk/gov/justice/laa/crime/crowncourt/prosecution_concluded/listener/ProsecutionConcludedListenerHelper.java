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

import org.slf4j.MDC;
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

            // Save a copy of the message
            queueMessageLogService.createLog(MessageType.PROSECUTION_CONCLUDED, message);

            // Validate and extract the MAAT ID so it can be tagged on all the log messages
            int maatId = prosecutionConcludedValidator.validateMaatId(message);
            MDC.put("maatId", String.valueOf(maatId));

            // Process the message
            prosecutionConcluded = gson.fromJson(message, ProsecutionConcluded.class);
            prosecutionConcludedService.execute(prosecutionConcluded);
            log.info("Prosecution concluded message processing is complete");

        } catch (ValidationException exception) {
            if (ProsecutionConcludedValidator.MAAT_ID_FORMAT_INCORRECT.equalsIgnoreCase(exception.getMessage())) {
                log.error("MAAT ID is missing or not a number.");
            } else {
                log.warn("Processing terminated by a validation exception: {}", exception.getMessage());
                log.info("Adding message to dead letter table");
                deadLetterMessageService.logDeadLetterMessage(exception.getMessage(), prosecutionConcluded);
            }
        } catch (Exception exception) {
            log.error("Unexpected error occurred", exception);
            // The error is unknown, throwing an exception will mean the message is retried by SQS
            throw new RuntimeException(exception);
        } finally {
            MDC.remove("maatId");
        }
    }
}
