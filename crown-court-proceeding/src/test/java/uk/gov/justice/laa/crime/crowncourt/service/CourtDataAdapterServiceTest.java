package uk.gov.justice.laa.crime.crowncourt.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.commons.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.config.MockServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtDataAdapterServiceTest {

    @Mock
    private RestAPIClient cdaAPIClient;

    @InjectMocks
    private CourtDataAdapterService courtDataAdapterService;

    @Spy
    private ServicesConfiguration configuration = MockServicesConfiguration.getConfiguration(1000);

    @Test
    void givenAValidHearingId_whenTriggerHearingProcessingIsInvoked_thenTheRequestIsSentCorrectly() {
        UUID testHearingId = UUID.randomUUID();
        String testTransactionId = UUID.randomUUID().toString();
        courtDataAdapterService.triggerHearingProcessing(testHearingId, testTransactionId);

        verify(cdaAPIClient).get(
                eq(Void.class),
                anyString(),
                anyMap(),
                ArgumentMatchers.any(),
                any(UUID.class)
        );
    }

    @Test
    void givenAValidHearingId_whenTriggerHearingProcessingIsInvokedAndTheCallFails_thenFailureIsHandled() {

        UUID testHearingId = UUID.randomUUID();
        String testTransactionId = UUID.randomUUID().toString();

        when(cdaAPIClient.get(
                eq(Void.class),
                anyString(),
                anyMap(),
                ArgumentMatchers.any(),
                any(UUID.class)
        )).thenThrow(new APIClientException());

        assertThatThrownBy(() -> courtDataAdapterService.triggerHearingProcessing(testHearingId, testTransactionId))
                .isInstanceOf(APIClientException.class);
    }
}
