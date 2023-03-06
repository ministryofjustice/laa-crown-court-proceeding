package uk.gov.justice.laa.crime.crowncourt.service;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
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
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.*;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateSentenceOrder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class MaatCourtDataServiceTest {

    private static final String LAA_TRANSACTION_ID = "laaTransactionId";
    @Mock
    MaatAPIClient maatCourtDataClient;
    @InjectMocks
    private MaatCourtDataService maatCourtDataService;
    @Spy
    private ServicesConfiguration configuration = MockServicesConfiguration.getConfiguration(1000);

    @Test
    void givenRepId_whenGetCurrentPassedIojAppealFromRepIdIsInvoked_thenResponseIsReturned() {
        IOJAppealDTO expected = new IOJAppealDTO();
        when(maatCourtDataClient.getApiResponseViaGET(any(), anyString(), anyMap(), any()))
                .thenReturn(expected);

        IOJAppealDTO response =
                maatCourtDataService.getCurrentPassedIOJAppealFromRepId(TestModelDataBuilder.TEST_REP_ID, LAA_TRANSACTION_ID);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void givenUpdateRepOrderRequest_whenUpdateRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateRepOrder(UpdateRepOrderRequestDTO.builder().build(), LAA_TRANSACTION_ID);
        verify(maatCourtDataClient).getApiResponseViaPUT(
                any(UpdateRepOrderRequestDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAValidRequest_whenUpdateRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateRepOrder(UpdateRepOrderRequestDTO.builder().build(), LAA_TRANSACTION_ID);
        verify(maatCourtDataClient).getApiResponseViaPUT(
                any(UpdateRepOrderRequestDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAValidRepId_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnOutcome() {
        maatCourtDataService.getRepOrderCCOutcomeByRepId(TestModelDataBuilder.TEST_REP_ID, LAA_TRANSACTION_ID);
        verify(maatCourtDataClient, atLeastOnce()).getApiResponseViaGET(any(), anyString(), anyMap(), any());
    }

    @Test
    void givenAValidRequest_whenCreateOutcomeIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.createOutcome(RepOrderCCOutcomeDTO.builder().build(), LAA_TRANSACTION_ID);
        verify(maatCourtDataClient).getApiResponseViaPUT(
                any(RepOrderCCOutcomeDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAInvalidParameter_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnError() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), anyString(), anyMap(), any())).thenThrow(new APIClientException());
        assertThatThrownBy(() -> maatCourtDataService.getRepOrderCCOutcomeByRepId(
                TestModelDataBuilder.TEST_REP_ID, LAA_TRANSACTION_ID)
        ).isInstanceOf(APIClientException.class);
    }

    @Test
    void givenAInvalidParameter_whenRetrieveHearingForCaseConclusionIsInvoked_thenNullIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(null);
        maatCourtDataService.retrieveHearingForCaseConclusion(ProsecutionConcluded.builder().build());
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenRetrieveHearingForCaseConclusionIsInvoked_thenEmptyIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(List.of());
        WQHearingDTO wqHearingDTO = maatCourtDataService.retrieveHearingForCaseConclusion(ProsecutionConcluded.builder().build());
        assertThat(wqHearingDTO).isNull();
    }

    @Test
    void givenAInvalidParameter_whenRetrieveHearingForCaseConclusionIsInvoked_thenResponseIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(List.of(WQHearingDTO.builder().build()));
        WQHearingDTO wqHearingDTO = maatCourtDataService.retrieveHearingForCaseConclusion(ProsecutionConcluded.builder().build());
        assertThat(wqHearingDTO).isNotNull();
    }

    @Test
    void givenAInvalidParameter_whenFindWQLinkRegisterByMaatIdIsInvoked_thenNullIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(null);
        int caseId = maatCourtDataService.findWQLinkRegisterByMaatId(TestModelDataBuilder.TEST_REP_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
        assertThat(caseId).isZero();
    }

    @Test
    void givenAInvalidParameter_whenFindWQLinkRegisterByMaatIdIsInvoked_thenEmptyIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(List.of());
        int caseId = maatCourtDataService.findWQLinkRegisterByMaatId(TestModelDataBuilder.TEST_REP_ID);
        assertThat(caseId).isZero();
    }

    @Test
    void givenAInvalidParameter_whenFindWQLinkRegisterByMaatIdIsInvoked_thenResponseIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(List.of(WQLinkRegisterDTO.builder()
                .caseId(TestModelDataBuilder.TEST_CASE_ID).build()));
        int caseId = maatCourtDataService.findWQLinkRegisterByMaatId(TestModelDataBuilder.TEST_REP_ID);
        assertThat(caseId).isEqualTo(TestModelDataBuilder.TEST_CASE_ID);
    }

    @Test
    void givenAInvalidParameter_whenFindOffenceByCaseIdIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.findOffenceByCaseId(TestModelDataBuilder.TEST_REP_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenGetOffenceNewOffenceCountIsInvoked_thenNullIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(null);
        int offenceCount = maatCourtDataService.getOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAInvalidParameter_whenGetOffenceNewOffenceCountIsInvoked_thenEmptyIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(List.of());
        int offenceCount = maatCourtDataService.getOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAInvalidParameter_whenGetOffenceNewOffenceCountIsInvoked_thenResponseIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(List.of(4));
        int offenceCount = maatCourtDataService.getOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
        assertThat(offenceCount).isEqualTo(4);
    }

    @Test
    void givenAInvalidParameter_whenGetWQOffenceNewOffenceCountIsInvoked_thenNullIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(null);
        int offenceCount = maatCourtDataService.getWQOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAInvalidParameter_whenGetWQOffenceNewOffenceCountIsInvoked_thenEmptyIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(List.of());
        int offenceCount = maatCourtDataService.getWQOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAInvalidParameter_whenGetWQOffenceNewOffenceCountIsInvoked_thenResponseIsReturned() {
        when(maatCourtDataClient.getApiResponseViaGET(any(), any(), any(), any())).thenReturn(List.of(5));
        int offenceCount = maatCourtDataService.getWQOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
        assertThat(offenceCount).isEqualTo(5);
    }

    @Test
    void givenAInvalidParameter_whenFindResultsByWQTypeSubTypeIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.findResultsByWQTypeSubType(1244, 25243);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenGetResultCodeByCaseIdAndAsnSeqIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.getResultCodeByCaseIdAndAsnSeq(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenGetWqResultCodeByCaseIdAndAsnSeqIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.getWqResultCodeByCaseIdAndAsnSeq(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenFetchResultCodesForCCImprisonmentIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.fetchResultCodesForCCImprisonment();
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenFindByCjsResultCodeInIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.findByCjsResultCodeIn();
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenGetRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.getRepOrder(TestModelDataBuilder.TEST_REP_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenUpdateCrownCourtOutcomeIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateCrownCourtOutcome(UpdateCCOutcome.builder().build());
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenInvokeUpdateAppealSentenceOrderDateIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.invokeUpdateAppealSentenceOrderDate(UpdateSentenceOrder.builder().build());
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenInvokeUpdateSentenceOrderDateIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.invokeUpdateSentenceOrderDate(UpdateSentenceOrder.builder().build());
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenMaatRecordLockedIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.isMaatRecordLocked(TestModelDataBuilder.TEST_REP_ID);
        verify(maatCourtDataClient).getApiResponseViaGET(any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenGetRepOrderByFilterIsInvoked_thenResponseIsReturned() throws Exception {
        maatCourtDataService.getRepOrderByFilter(TestModelDataBuilder.TEST_REP_ID.toString(),
                TestModelDataBuilder.TEST_SENTENCE_ORDER_DATE.toString());
        verify(maatCourtDataClient).getGraphQLApiResponse(any(), any(), any());
    }
}