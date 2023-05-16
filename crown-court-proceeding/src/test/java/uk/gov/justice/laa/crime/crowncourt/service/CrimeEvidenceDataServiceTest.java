package uk.gov.justice.laa.crime.crowncourt.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.commons.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.config.MockServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrimeEvidenceDataServiceTest {

    @Mock
    private RestAPIClient evidenceAPIClient;

    @InjectMocks
    private CrimeEvidenceDataService crimeEvidenceDataService;

    @Spy
    private ServicesConfiguration configuration = MockServicesConfiguration.getConfiguration(1000);

    @Test
    void givenAValidEvidenceFeeRequest_whenGetCalEvidenceFeeIsInvoked_thenReturnEvidenceFeeResponse() {
        crimeEvidenceDataService.getCalEvidenceFee(TestModelDataBuilder.getApiCalculateEvidenceFeeRequest());
        verify(evidenceAPIClient).post(any(), any(), any(), any());

    }

    @Test
    void givenAValidEvidenceFeeRequest_whenGetCalEvidenceFeeIsInvokedAndTheApiCallFails_thenFailureIsHandled() {

        when(evidenceAPIClient.post(any(), any(), any(), any())).thenThrow(new APIClientException());
        assertThatThrownBy(() -> {
            crimeEvidenceDataService.getCalEvidenceFee(TestModelDataBuilder.getApiCalculateEvidenceFeeRequest());
        }).isInstanceOf(APIClientException.class);
    }

}