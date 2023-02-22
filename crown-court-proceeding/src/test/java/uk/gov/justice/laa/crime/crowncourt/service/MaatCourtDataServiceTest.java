package uk.gov.justice.laa.crime.crowncourt.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.client.MaatAPIClient;
import uk.gov.justice.laa.crime.crowncourt.config.MockServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaatCourtDataServiceTest {

    private static final String LAA_TRANSACTION_ID = "laaTransactionId";

    @Mock
    MaatAPIClient maatAPIClient;

    @InjectMocks
    private MaatCourtDataService maatCourtDataService;

    @Spy
    private ServicesConfiguration configuration = MockServicesConfiguration.getConfiguration(1000);

    @Test
    void givenRepId_whenGetCurrentPassedIojAppealFromRepIdIsInvoked_thenResponseIsReturned() {
        IOJAppealDTO expected = new IOJAppealDTO();
        when(maatAPIClient.getApiResponseViaGET(any(), anyString(), anyMap(), any()))
                .thenReturn(expected);

        IOJAppealDTO response =
                maatCourtDataService.getCurrentPassedIOJAppealFromRepId(TestModelDataBuilder.TEST_REP_ID, LAA_TRANSACTION_ID);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void givenUpdateRepOrderRequest_whenUpdateRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateRepOrder(UpdateRepOrderRequestDTO.builder().build(), LAA_TRANSACTION_ID);
        verify(maatAPIClient).getApiResponseViaPUT(
                any(UpdateRepOrderRequestDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAValidRequest_whenUpdateRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateRepOrder(UpdateRepOrderRequestDTO.builder().build(), LAA_TRANSACTION_ID);
        verify(maatAPIClient).getApiResponseViaPUT(
                any(UpdateRepOrderRequestDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAValidRepId_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnOutcome() {
        maatCourtDataService.getRepOrderCCOutcomeByRepId(TestModelDataBuilder.TEST_REP_ID, LAA_TRANSACTION_ID);
        verify(maatAPIClient, atLeastOnce()).getApiResponseViaGET(any(), anyString(), anyMap(), any());
    }

    @Test
    void givenAValidRequest_whenCreateOutcomeIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.createOutcome(RepOrderCCOutcomeDTO.builder().build(), LAA_TRANSACTION_ID);
        verify(maatAPIClient).getApiResponseViaPUT(
                any(RepOrderCCOutcomeDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAInvalidParameter_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnError() {
        when(maatAPIClient.getApiResponseViaGET(any(), anyString(), anyMap(), any())).thenThrow(new APIClientException());
        assertThatThrownBy(() -> {
            maatCourtDataService.getRepOrderCCOutcomeByRepId(TestModelDataBuilder.TEST_REP_ID, LAA_TRANSACTION_ID);
        }).isInstanceOf(APIClientException.class);
    }
}