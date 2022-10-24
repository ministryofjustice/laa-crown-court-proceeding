package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.listener;


import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedListenerTest {

    @InjectMocks
    private ProsecutionConcludedListener prosecutionConcludedListener;

    @Test
    void givenJSONMessageIsReceived_whenProsecutionConcludedListenerIsInvoked_thenReceiveIsCalled() {
        prosecutionConcludedListener.receive(getSqsMessagePayload());
    }

    private String getSqsMessagePayload() {
        return """
                prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                hearingIdWhereChangeOccurred : 61600a90-89e2-4717-aa9b-a01fc66130c1
                """;
    }

}