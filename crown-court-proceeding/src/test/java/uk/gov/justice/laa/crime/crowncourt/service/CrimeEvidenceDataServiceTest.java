package uk.gov.justice.laa.crime.crowncourt.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.crime.crowncourt.client.EvidenceApiClient;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CrimeEvidenceDataServiceTest {

    @Mock
    private EvidenceApiClient evidenceAPIClient;

    @InjectMocks
    private CrimeEvidenceDataService crimeEvidenceDataService;

    @Test
    void givenAValidEvidenceFeeRequest_whenGetCalEvidenceFeeIsInvoked_thenReturnEvidenceFeeResponse() {
        crimeEvidenceDataService.getCalculateEvidenceFee(TestModelDataBuilder.getApiCalculateEvidenceFeeRequest());
        verify(evidenceAPIClient).calculateEvidenceFee(any());

    }

    @Test
    void givenAValidEvidenceFeeRequest_whenGetCalEvidenceFeeIsInvokedAndTheApiCallFails_thenFailureIsHandled() {

        doThrow(WebClientResponseException.class)
            .when(evidenceAPIClient).calculateEvidenceFee(any());
        
        assertThatThrownBy(() -> crimeEvidenceDataService.getCalculateEvidenceFee(
                TestModelDataBuilder.getApiCalculateEvidenceFeeRequest())
        ).isInstanceOf(WebClientResponseException.class);
    }

}