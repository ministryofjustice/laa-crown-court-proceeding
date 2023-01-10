package uk.gov.justice.laa.crime.crowncourt.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.entity.QueueMessageLogEntity;
import uk.gov.justice.laa.crime.crowncourt.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.repository.QueueMessageLogRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.justice.laa.crime.crowncourt.enums.MessageType.LAA_STATUS_UPDATE;

@Service
@Slf4j
@XRayEnabled
@RequiredArgsConstructor
public class QueueMessageLogService {

    private final QueueMessageLogRepository queueMessageLogRepository;

    public void createLog(final MessageType messageType, final String message) {

        JsonObject msgObject = JsonParser.parseString(message).getAsJsonObject();

        JsonElement laaTransactionUUID = msgObject.has("metadata") ?
                msgObject.get("metadata").getAsJsonObject().get("laaTransactionId") :
                msgObject.get("laaTransactionId");

        QueueMessageLogEntity queueMessageLogEntity =
                QueueMessageLogEntity.builder()
                        .transactionUUID(UUID.randomUUID().toString())
                        .laaTransactionId(Optional.ofNullable(laaTransactionUUID).map(JsonElement::getAsString)
                                .orElse(null))
                        .maatId(-1)
                        .type(prepareMessageType(messageType, msgObject))
                        .message(convertAsByte(message))
                        .createdTime(LocalDateTime.now())
                        .build();

        queueMessageLogRepository.save(queueMessageLogEntity);
    }


    private String prepareMessageType(MessageType messageType, JsonObject msgObject) {

        final JsonElement jurType = msgObject.get("jurisdictionType");
        final StringBuilder msgBuilder = new StringBuilder().append(messageType.name());

        Optional<String> jurisdiction = Optional.ofNullable(jurType).map(JsonElement::getAsString);

        if (jurisdiction.isPresent()) {
            msgBuilder
                    .append("-")
                    .append(jurisdiction.get());
        }

        return msgBuilder.toString();
    }


    private byte[] convertAsByte(final String message) {

        return Optional.ofNullable(message).isPresent() ?
                message.getBytes() : null;
    }
}
