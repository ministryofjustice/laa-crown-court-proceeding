package uk.gov.justice.laa.crime.crowncourt.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.entity.QueueMessageLogEntity;
import uk.gov.justice.laa.crime.crowncourt.repository.QueueMessageLogRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueueMessageLogService {

    private final QueueMessageLogRepository queueMessageLogRepository;

    public void createLog(final MessageType messageType, final String message) {

        if (StringUtils.isNotEmpty(message)) {
            JsonObject msgObject = JsonParser.parseString(message).getAsJsonObject();
            JsonElement maatId = msgObject.get("maatId");

            JsonElement laaTransactionUUID = msgObject.has("metadata") ?
                    msgObject.get("metadata").getAsJsonObject().get("laaTransactionId") :
                    msgObject.get("laaTransactionId");

            QueueMessageLogEntity queueMessageLogEntity =
                    QueueMessageLogEntity.builder()
                            .transactionUUID(UUID.randomUUID().toString())
                            .laaTransactionId(Optional.ofNullable(laaTransactionUUID).map(JsonElement::getAsString)
                                    .orElse(null))
                            .maatId(Optional
                                    .ofNullable(maatId)
                                    .map(JsonElement::getAsInt)
                                    .orElse(-1))
                            .type(prepareMessageType(messageType, msgObject))
                            .message(convertAsByte(message))
                            .createdTime(LocalDateTime.now())
                            .build();

            queueMessageLogRepository.save(queueMessageLogEntity);
        } else {
            log.error("Log Message is Empty");
        }

    }


    private String prepareMessageType(MessageType messageType, JsonObject msgObject) {

        final JsonElement jurType = msgObject.get("jurisdictionType");
        final StringBuilder msgBuilder = new StringBuilder().append(messageType.name());

        Optional<String> jurisdiction = Optional.ofNullable(jurType).map(JsonElement::getAsString);

        jurisdiction.ifPresent(s -> msgBuilder
                .append("-")
                .append(s));

        return msgBuilder.toString();
    }


    private byte[] convertAsByte(final String message) {
        return message.getBytes();
    }
}
