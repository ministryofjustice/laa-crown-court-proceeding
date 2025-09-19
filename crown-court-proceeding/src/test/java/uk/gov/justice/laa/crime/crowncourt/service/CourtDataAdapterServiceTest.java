package uk.gov.justice.laa.crime.crowncourt.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.client.CourtDataAdaptorNonServletApiClient;

import java.util.UUID;

import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAdapterService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtDataAdapterServiceTest {

    @Mock
    CourtDataAdaptorNonServletApiClient cdaAPIClient;

    @InjectMocks
    private CourtDataAdapterService courtDataAdapterService;
    
    @Test
    void givenAValidHearingId_whenTriggerHearingProcessingIsInvoked_thenTheRequestIsSentCorrectly() {
        UUID testHearingId = UUID.randomUUID();

        courtDataAdapterService.triggerHearingProcessing(testHearingId);

        verify(cdaAPIClient).triggerHearingProcessing(
            eq(testHearingId),
            argThat(params -> "true".equals(params.getFirst("publish_to_queue")))
        );
    }

    @Test
    void givenAValidHearingId_whenTriggerHearingProcessingIsInvokedAndTheCallFails_thenFailureIsHandled() {

        UUID testHearingId = UUID.randomUUID();

        doThrow(WebClientResponseException.class)
            .when(cdaAPIClient).triggerHearingProcessing(eq(testHearingId), 
                argThat(params -> "true".equals(params.getFirst("publish_to_queue"))));
        
        assertThatThrownBy(() -> courtDataAdapterService.triggerHearingProcessing(testHearingId))
                .isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void givenAValidHearingId_whenGetHearingIsInvoked_thenTheRequestIsSentCorrectly() {
        UUID testHearingId = UUID.randomUUID();

        courtDataAdapterService.getHearingResult(ProsecutionConcluded.builder().hearingIdWhereChangeOccurred(testHearingId).build());

        verify(cdaAPIClient).getHearingResult(
                eq(testHearingId),
                argThat(params -> "false".equals(params.getFirst("publish_to_queue")))
        );
    }

    @Test
    void givenAValidHearingId_whenGetHearingIsInvokedAndTheCallFails_thenFailureIsHandled() {

        UUID testHearingId = UUID.randomUUID();

        doThrow(WebClientResponseException.class)
                .when(cdaAPIClient).getHearingResult(eq(testHearingId),
                        argThat(params -> "false".equals(params.getFirst("publish_to_queue"))));

        assertThatThrownBy(() -> courtDataAdapterService.getHearingResult(ProsecutionConcluded.builder().hearingIdWhereChangeOccurred(testHearingId).build()))
                .isInstanceOf(WebClientResponseException.class);
    }
}
