package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "feature.postMvp.enabled", havingValue = "true")
public class ProsecutionConcludedListener {
    private final Gson gson;

    private final ProsecutionConcludedService prosecutionConcludedService;

    private final QueueMessageLogService queueMessageLogService;

    @JmsListener(destination = "${cloud-platform.aws.sqs.queue.prosecutionConcluded}")
    public void receive(@Payload final String message) {

        queueMessageLogService.createLog(MessageType.PROSECUTION_CONCLUDED, message);

        ProsecutionConcluded prosecutionConcluded = gson.fromJson(message, ProsecutionConcluded.class);
        prosecutionConcludedService.execute(prosecutionConcluded);

        log.info("CC Outcome is completed for  maat-id {}", prosecutionConcluded.getMaatId());
    }
}