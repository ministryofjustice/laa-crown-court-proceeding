package uk.gov.justice.laa.crime.crowncourt.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.crowncourt.client.EvidenceApiClient;
import uk.gov.justice.laa.crime.crowncourt.client.MaatCourtDataApiClient;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class MaatCourtDataServiceTest {

    @Mock
    MaatCourtDataApiClient maatAPIClient;

    @Mock
    EvidenceApiClient evidenceApiClient;

    @InjectMocks
    private MaatCourtDataService maatCourtDataService;

    @InjectMocks
    private CrimeEvidenceDataService evidenceService;

    @Test
    void givenRepId_whenGetCurrentPassedIojAppealFromRepIdIsInvoked_thenResponseIsReturned() {
        IOJAppealDTO expected = new IOJAppealDTO();
        when(maatAPIClient.getCurrentPassedIOJAppeal(any())).thenReturn(expected);

        IOJAppealDTO response =
                maatCourtDataService.getCurrentPassedIOJAppealFromRepId(TestModelDataBuilder.TEST_REP_ID);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void givenUpdateRepOrderRequest_whenUpdateRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateRepOrder(UpdateRepOrderRequestDTO.builder().build());
        verify(maatAPIClient).updateRepOrder(any(UpdateRepOrderRequestDTO.class));
    }

    @Test
    void givenAValidRepId_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnOutcome() {
        maatCourtDataService.getRepOrderCCOutcomeByRepId(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient, atLeastOnce()).getRepOrderCCOutcomeByRepId(any());
    }

    @Test
    void givenAValidRequest_whenCreateOutcomeIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.createOutcome(RepOrderCCOutcomeDTO.builder().build());
        verify(maatAPIClient).createCrownCourtOutcome(any(RepOrderCCOutcomeDTO.class));
    }

    @Test
    void givenAInvalidParameter_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnError() {
        when(maatAPIClient.getRepOrderCCOutcomeByRepId(any())).thenThrow(WebClientResponseException.class);
        assertThatThrownBy(() -> maatCourtDataService.getRepOrderCCOutcomeByRepId(TestModelDataBuilder.TEST_REP_ID))
                .isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void givenAValidParameter_whenOutcomeCountIsInvoked_thenResponseIsReturned() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);
        when(maatAPIClient.getOutcomeCount(any())).thenReturn(response);
        maatCourtDataService.outcomeCount(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient).getOutcomeCount(any());
    }

    @Test
    void givenAValidEvidenceFeeRequest_whenOutcomeCountIsInvokedAndNullIsReturned_then0IsReturned() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Records", "0");
        ResponseEntity<Void> response = new ResponseEntity<>(null, headers, HttpStatus.OK);
        when(maatAPIClient.getOutcomeCount(any())).thenReturn(response);
        long outcomeCount = maatCourtDataService.outcomeCount(TestModelDataBuilder.TEST_REP_ID);
        assertThat(outcomeCount).isZero();
    }

    @Test
    void givenAValidEvidenceFeeRequest_whenCalculateEvidenceFeeIsInvokedAndTheApiCallFails_thenFailureIsHandled() {
        when(maatAPIClient.getOutcomeCount(anyInt())).thenThrow(WebClientResponseException.class);
        assertThatThrownBy(() -> maatCourtDataService.outcomeCount(TestModelDataBuilder.TEST_REP_ID))
                .isInstanceOf(WebClientResponseException.class);
        verify(maatAPIClient).getOutcomeCount(any());
    }
}
