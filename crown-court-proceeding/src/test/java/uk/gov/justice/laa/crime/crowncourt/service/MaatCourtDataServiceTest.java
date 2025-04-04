package uk.gov.justice.laa.crime.crowncourt.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.commons.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.config.MockServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class MaatCourtDataServiceTest {

    @Mock
    RestAPIClient maatAPIClient;

    @InjectMocks
    private MaatCourtDataService maatCourtDataService;

    @Spy
    private ServicesConfiguration configuration = MockServicesConfiguration.getConfiguration(1000);

    @Test
    void givenRepId_whenGetCurrentPassedIojAppealFromRepIdIsInvoked_thenResponseIsReturned() {
        IOJAppealDTO expected = new IOJAppealDTO();
        when(maatAPIClient.get(any(), anyString(), anyMap(), any()))
                .thenReturn(expected);

        IOJAppealDTO response =
                maatCourtDataService.getCurrentPassedIOJAppealFromRepId(
                        TestModelDataBuilder.TEST_REP_ID
                );

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void givenUpdateRepOrderRequest_whenUpdateRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateRepOrder(UpdateRepOrderRequestDTO.builder().build());
        verify(maatAPIClient).put(
                any(UpdateRepOrderRequestDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAValidRepId_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnOutcome() {
        maatCourtDataService.getRepOrderCCOutcomeByRepId(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient, atLeastOnce()).get(any(), anyString(), anyMap(), any());
    }

    @Test
    void givenAValidRequest_whenCreateOutcomeIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.createOutcome(RepOrderCCOutcomeDTO.builder().build());
        verify(maatAPIClient).post(
                any(RepOrderCCOutcomeDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAInvalidParameter_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnError() {
        when(maatAPIClient.get(any(), anyString(), anyMap(), any()))
                .thenThrow(new APIClientException());
        assertThatThrownBy(() -> maatCourtDataService.getRepOrderCCOutcomeByRepId(
                TestModelDataBuilder.TEST_REP_ID)
        ).isInstanceOf(APIClientException.class);
    }

    @Test
    void givenAValidParameter_whenOutcomeCountIsInvoked_thenResponseIsReturned() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);
        when(maatAPIClient.head(any(), any(), any())).thenReturn(response);
        maatCourtDataService.outcomeCount(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient).head(any(), any(), any());
    }

    @Test
    void givenAValidEvidenceFeeRequest_whenGetCalEvidenceFeeIsInvokedAndTheApiCallFails_thenFailureIsHandled() {

        when(maatAPIClient.head(any(), any(), any())).thenThrow(new APIClientException());

        assertThatThrownBy(() -> maatCourtDataService.outcomeCount(
                TestModelDataBuilder.TEST_REP_ID)
        ).isInstanceOf(APIClientException.class);
    }

    @Test
    void givenAValidEvidenceFeeRequest_whenOutcomeCountIsInvokedAndNullIsReturned_then0IsReturned() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Records", "0");
        ResponseEntity<Void> response = new ResponseEntity<>(null, headers, HttpStatus.OK);
        when(maatAPIClient.head(any(), any(), any())).thenReturn(response);
        long outcomeCount = maatCourtDataService.outcomeCount(TestModelDataBuilder.TEST_REP_ID);
        assertThat(outcomeCount).isZero();
    }
}