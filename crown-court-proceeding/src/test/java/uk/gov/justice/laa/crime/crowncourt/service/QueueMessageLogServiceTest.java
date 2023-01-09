package uk.gov.justice.laa.crime.crowncourt.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.entity.QueueMessageLogEntity;
import uk.gov.justice.laa.crime.crowncourt.enums.JurisdictionType;
import uk.gov.justice.laa.crime.crowncourt.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.repository.QueueMessageLogRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class QueueMessageLogServiceTest {

    @InjectMocks
    public QueueMessageLogService queueMessageLogService;
    @Spy
    public QueueMessageLogRepository queueMessageLogRepository;
    @Captor
    private ArgumentCaptor<QueueMessageLogEntity> queueMessageCaptor;



    @Test
    public void testWhenProsecutionConcluded_thenCheckLogEntryCreated() {

        final Integer maatId = 1000;
        queueMessageLogService.createLog(MessageType.PROSECUTION_CONCLUDED, getQueueMessage(maatId, JurisdictionType.CROWN));

        verify(queueMessageLogRepository, times(1)).save(queueMessageCaptor.capture());
        QueueMessageLogEntity savedQueueMsg = queueMessageCaptor.getValue();

        assertAll("linkMessage",
                () -> assertNotNull(savedQueueMsg),
                () -> assertNotNull(savedQueueMsg.getMaatId()),
                () -> assertNotNull(savedQueueMsg.getTransactionUUID()),
                () -> assertNotNull(savedQueueMsg.getLaaTransactionId()),
                () -> assertNotNull(savedQueueMsg.getType()),
                () -> assertNotNull(savedQueueMsg.getMessage()),
                () -> assertNotNull(savedQueueMsg.getCreatedTime()),
                () -> assertEquals("8720c683-39ef-4168-a8cc-058668a2dcca", savedQueueMsg.getLaaTransactionId()),
                () -> assertEquals(savedQueueMsg.getMaatId(), maatId)
        );
    }

    private String getQueueMessage(Integer maatId, JurisdictionType jurisdictionType) {

        return  "{" +
                "    \"maatId\": " + maatId + ",\n" +
                "    \"jurisdictionType\": " + jurisdictionType.name() + ",\n" +
                "    \"metadata\": {\n" +
                "        \"laaTransactionId\":\"8720c683-39ef-4168-a8cc-058668a2dcca\" \n" +
                "    }\n" +
                "}";

    }
}

