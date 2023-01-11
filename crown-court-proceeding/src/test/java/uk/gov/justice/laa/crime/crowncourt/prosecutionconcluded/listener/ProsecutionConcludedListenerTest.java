package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.listener;


import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedListenerTest {

    @InjectMocks
    private ProsecutionConcludedListener prosecutionConcludedListener;

    @Mock
    private Gson gson;
    @Mock
    private QueueMessageLogService queueMessageLogService;

    @Test
    void givenJSONMessageIsReceived_whenProsecutionConcludedListenerIsInvoked_thenReceiveIsCalled() {
        String message = getSqsMessagePayload();
        prosecutionConcludedListener.receive(message);
        verify(queueMessageLogService).createLog(MessageType.PROSECUTION_CONCLUDED, message);
        assertThat(Boolean.TRUE).isTrue();
    }

    private String getSqsMessagePayload() {
        return """
                prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                hearingIdWhereChangeOccurred : 61600a90-89e2-4717-aa9b-a01fc66130c1
                """;
    }

}