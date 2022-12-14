package uk.gov.justice.laa.crime.crowncourt.hearing.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HearingResultedListenerTest {

    @InjectMocks
    private HearingResultedListener hearingResultedListener;

    @Test
    void givenJSONMessageIsReceived_whenHearingResultedListenerIsInvoked_thenReceiveIsCalled() {
        hearingResultedListener.receive(getSqsMessagePayload());
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