package uk.gov.justice.laa.crime.crowncourt.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import uk.gov.justice.laa.crime.crowncourt.entity.QueueMessageLogEntity;
import uk.gov.justice.laa.crime.crowncourt.repository.QueueMessageLogRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueueMessageLogServiceTest {

    @InjectMocks
    private QueueMessageLogService queueMessageLogService;

    @Spy
    private QueueMessageLogRepository queueMessageLogRepository;

    @Captor
    private ArgumentCaptor<QueueMessageLogEntity> queueMessageCaptor;

    @Test
    void testWhenProsecutionConcluded_thenCheckLogEntryCreated() {

        final Integer maatId = 1000;
        queueMessageLogService.createLog(
                MessageType.PROSECUTION_CONCLUDED, getQueueMessage(maatId, JurisdictionType.CROWN));

        verify(queueMessageLogRepository, times(1)).save(queueMessageCaptor.capture());
        QueueMessageLogEntity savedQueueMsg = queueMessageCaptor.getValue();

        assertAll(
                "ProsecutionMessage",
                () -> assertNotNull(savedQueueMsg),
                () -> assertNotNull(savedQueueMsg.getMaatId()),
                () -> assertNotNull(savedQueueMsg.getTransactionUUID()),
                () -> assertNotNull(savedQueueMsg.getLaaTransactionId()),
                () -> assertNotNull(savedQueueMsg.getType()),
                () -> assertNotNull(savedQueueMsg.getMessage()),
                () -> assertNotNull(savedQueueMsg.getCreatedTime()),
                () -> assertEquals("8720c683-39ef-4168-a8cc-058668a2dcca", savedQueueMsg.getLaaTransactionId()),
                () -> assertEquals(savedQueueMsg.getMaatId(), maatId));
    }

    @Test
    void testWhenProsecutionConcludedMessageIsNull_thenCheckLogEntryNotCreated() {
        queueMessageLogService.createLog(MessageType.PROSECUTION_CONCLUDED, "");
        verify(queueMessageLogRepository, times(0)).save(queueMessageCaptor.capture());
    }

    @Test
    void testWhenMetadataIsNull_thenProcessAsExpected() {

        final Integer maatId = 1000;

        queueMessageLogService.createLog(MessageType.PROSECUTION_CONCLUDED, newQueueMessageWithoutMetaData(maatId));

        verify(queueMessageLogRepository).save(queueMessageCaptor.capture());

        QueueMessageLogEntity savedQueueMsg = queueMessageCaptor.getValue();

        assertAll(
                "SQSMessage",
                () -> assertNotNull(savedQueueMsg),
                () -> assertNotNull(savedQueueMsg.getMaatId()),
                () -> assertNotNull(savedQueueMsg.getTransactionUUID()),
                () -> assertNull(savedQueueMsg.getLaaTransactionId()),
                () -> assertNotNull(savedQueueMsg.getType()),
                () -> assertNotNull(savedQueueMsg.getMessage()),
                () -> assertEquals(savedQueueMsg.getMaatId(), maatId));
    }

    private String newQueueMessageWithoutMetaData(Integer maatId) {

        return "{" + "    \"maatId\": " + maatId + "\n" + "}";
    }

    private String getQueueMessage(Integer maatId, JurisdictionType jurisdictionType) {

        return "{" + "    \"maatId\": "
                + maatId + ",\n" + "    \"jurisdictionType\": "
                + jurisdictionType.name() + ",\n" + "    \"metadata\": {\n"
                + "        \"laaTransactionId\":\"8720c683-39ef-4168-a8cc-058668a2dcca\" \n"
                + "    }\n"
                + "}";
    }

    @Test
    void givenAValidInput_whenPurgePeriodicMessagesIsInvoked_thenPurgeProcessIsSuccess() {
        queueMessageLogService.purgePeriodicMessages();
        verify(queueMessageLogRepository).deleteByCreatedTimeBefore(any(LocalDateTime.class));
    }
}
