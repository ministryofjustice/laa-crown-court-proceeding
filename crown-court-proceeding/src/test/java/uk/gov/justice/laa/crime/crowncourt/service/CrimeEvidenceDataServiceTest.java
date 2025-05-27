package uk.gov.justice.laa.crime.crowncourt.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.crime.crowncourt.client.EvidenceApiClient;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;

@ExtendWith(MockitoExtension.class)
class CrimeEvidenceDataServiceTest {

    @Mock private EvidenceApiClient evidenceAPIClient;

    @InjectMocks private CrimeEvidenceDataService crimeEvidenceDataService;

    @Test
    void
            givenAValidEvidenceFeeRequest_whenCalculateEvidenceFeeIsInvoked_thenReturnEvidenceFeeResponse() {
        crimeEvidenceDataService.calculateEvidenceFee(
                TestModelDataBuilder.getApiCalculateEvidenceFeeRequest());
        verify(evidenceAPIClient).calculateEvidenceFee(any());
    }

    @Test
    void
            givenAValidEvidenceFeeRequest_whenCalculateEvidenceFeeIsInvokedAndTheApiCallFails_thenFailureIsHandled() {
        when(evidenceAPIClient.calculateEvidenceFee(any()))
                .thenThrow(WebClientResponseException.class);

        assertThatThrownBy(
                        () ->
                                crimeEvidenceDataService.calculateEvidenceFee(
                                        TestModelDataBuilder.getApiCalculateEvidenceFeeRequest()))
                .isInstanceOf(WebClientResponseException.class);
    }
}
