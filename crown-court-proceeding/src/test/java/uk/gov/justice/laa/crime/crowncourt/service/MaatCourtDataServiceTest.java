package uk.gov.justice.laa.crime.crowncourt.service;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.commons.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.config.MockServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.*;
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

    @Mock
    RestAPIClient maatAPIClient;

    @InjectMocks
    private MaatCourtDataService maatCourtDataService;

    @Spy
    private ServicesConfiguration configuration = MockServicesConfiguration.getConfiguration(1000);

    private static final String LAA_TRANSACTION_ID = "laaTransactionId";

    @Test
    void givenRepId_whenGetCurrentPassedIojAppealFromRepIdIsInvoked_thenResponseIsReturned() {
        IOJAppealDTO expected = new IOJAppealDTO();
        when(maatAPIClient.get(any(), anyString(), anyMap(), any()))
                .thenReturn(expected);

        IOJAppealDTO response =
                maatCourtDataService.getCurrentPassedIOJAppealFromRepId(TestModelDataBuilder.TEST_REP_ID, LAA_TRANSACTION_ID);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void givenUpdateRepOrderRequest_whenUpdateRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateRepOrder(UpdateRepOrderRequestDTO.builder().build(), LAA_TRANSACTION_ID);
        verify(maatAPIClient).put(
                any(UpdateRepOrderRequestDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAValidRequest_whenUpdateRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateRepOrder(UpdateRepOrderRequestDTO.builder().build(), LAA_TRANSACTION_ID);
        verify(maatAPIClient).put(
                any(UpdateRepOrderRequestDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAValidRepId_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnOutcome() {
        maatCourtDataService.getRepOrderCCOutcomeByRepId(TestModelDataBuilder.TEST_REP_ID, LAA_TRANSACTION_ID);
        verify(maatAPIClient, atLeastOnce()).get(any(), anyString(), anyMap(), any());
    }

    @Test
    void givenAValidRequest_whenCreateOutcomeIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.createOutcome(RepOrderCCOutcomeDTO.builder().build(), LAA_TRANSACTION_ID);
        verify(maatAPIClient).post(
                any(RepOrderCCOutcomeDTO.class),
                any(),
                anyString(),
                anyMap()
        );
    }

    @Test
    void givenAInvalidParameter_whenGetRepOrderCCOutcomeByRepIdIsInvoked_thenReturnError() {
        when(maatAPIClient.get(any(), anyString(), anyMap(), any())).thenThrow(new APIClientException());
        assertThatThrownBy(() -> maatCourtDataService.getRepOrderCCOutcomeByRepId(
                TestModelDataBuilder.TEST_REP_ID, LAA_TRANSACTION_ID)
        ).isInstanceOf(APIClientException.class);
    }

    @Test
    void givenAInvalidParameter_whenRetrieveHearingForCaseConclusionIsInvoked_thenNullIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(null);
        maatCourtDataService.retrieveHearingForCaseConclusion(ProsecutionConcluded.builder().build());
        verify(maatAPIClient).get(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenRetrieveHearingForCaseConclusionIsInvoked_thenEmptyIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(List.of());
        WQHearingDTO wqHearingDTO = maatCourtDataService.retrieveHearingForCaseConclusion(ProsecutionConcluded.builder().build());
        assertThat(wqHearingDTO).isNull();
    }

    @Test
    void givenAValidParameter_whenRetrieveHearingForCaseConclusionIsInvoked_thenResponseIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(List.of(WQHearingDTO.builder().build()));
        WQHearingDTO wqHearingDTO = maatCourtDataService.retrieveHearingForCaseConclusion(ProsecutionConcluded.builder().build());
        assertThat(wqHearingDTO).isNotNull();
    }

    @Test
    void givenAInvalidParameter_whenFindWQLinkRegisterByMaatIdIsInvoked_thenNullIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(null);
        int caseId = maatCourtDataService.findWQLinkRegisterByMaatId(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
        assertThat(caseId).isZero();
    }

    @Test
    void givenAInvalidParameter_whenFindWQLinkRegisterByMaatIdIsInvoked_thenEmptyIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(List.of());
        int caseId = maatCourtDataService.findWQLinkRegisterByMaatId(TestModelDataBuilder.TEST_REP_ID);
        assertThat(caseId).isZero();
    }

    @Test
    void givenAValidParameter_whenFindWQLinkRegisterByMaatIdIsInvoked_thenResponseIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(List.of(WQLinkRegisterDTO.builder()
                .caseId(TestModelDataBuilder.TEST_CASE_ID).build()));
        int caseId = maatCourtDataService.findWQLinkRegisterByMaatId(TestModelDataBuilder.TEST_REP_ID);
        assertThat(caseId).isEqualTo(TestModelDataBuilder.TEST_CASE_ID);
    }

    @Test
    void givenAValidParameter_whenFindOffenceByCaseIdIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.findOffenceByCaseId(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenGetOffenceNewOffenceCountIsInvoked_thenNullIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(null);
        int offenceCount = maatCourtDataService.getOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAInvalidParameter_whenGetOffenceNewOffenceCountIsInvoked_thenEmptyIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(List.of());
        int offenceCount = maatCourtDataService.getOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAValidParameter_whenGetOffenceNewOffenceCountIsInvoked_thenResponseIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(List.of(4));
        int offenceCount = maatCourtDataService.getOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
        assertThat(offenceCount).isEqualTo(4);
    }

    @Test
    void givenAInvalidParameter_whenGetWQOffenceNewOffenceCountIsInvoked_thenNullIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(null);
        int offenceCount = maatCourtDataService.getWQOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAInvalidParameter_whenGetWQOffenceNewOffenceCountIsInvoked_thenEmptyIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(List.of());
        int offenceCount = maatCourtDataService.getWQOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAValidParameter_whenGetWQOffenceNewOffenceCountIsInvoked_thenResponseIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any())).thenReturn(List.of(5));
        int offenceCount = maatCourtDataService.getWQOffenceNewOffenceCount(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
        assertThat(offenceCount).isEqualTo(5);
    }

    @Test
    void givenAValidParameter_whenFindResultsByWQTypeSubTypeIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.findResultsByWQTypeSubType(1244, 25243);
        verify(maatAPIClient).get(any(), any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenGetResultCodeByCaseIdAndAsnSeqIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.getResultCodeByCaseIdAndAsnSeq(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenGetWqResultCodeByCaseIdAndAsnSeqIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.getWqResultCodeByCaseIdAndAsnSeq(TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenFetchResultCodesForCCImprisonmentIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.fetchResultCodesForCCImprisonment();
        verify(maatAPIClient).get(any(), any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenFindByCjsResultCodeInIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.findByCjsResultCodeIn();
        verify(maatAPIClient).get(any(), any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenGetRepOrderIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.getRepOrder(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient).get(any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenUpdateCrownCourtOutcomeIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.updateCrownCourtOutcome(UpdateCCOutcome.builder().build());
        verify(maatAPIClient).get(any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenInvokeUpdateAppealSentenceOrderDateIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.invokeUpdateAppealSentenceOrderDate(UpdateSentenceOrder.builder().build());
        verify(maatAPIClient).get(any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenInvokeUpdateSentenceOrderDateIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.invokeUpdateSentenceOrderDate(UpdateSentenceOrder.builder().build());
        verify(maatAPIClient).get(any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenMaatRecordLockedIsInvoked_thenResponseIsReturned() {
        maatCourtDataService.isMaatRecordLocked(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient).get(any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenOutcomeCountIsInvoked_thenResponseIsReturned() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);
        when(maatAPIClient.head(any(), any(), any())).thenReturn(response);
        maatCourtDataService.outcomeCount(TestModelDataBuilder.TEST_REP_ID, TestModelDataBuilder.MEANS_ASSESSMENT_TRANSACTION_ID);
        verify(maatAPIClient).head(any(), any(), any());
    }

    @Test
    void givenAValidEvidenceFeeRequest_whenGetCalEvidenceFeeIsInvokedAndTheApiCallFails_thenFailureIsHandled() {

        when(maatAPIClient.head(any(), any(), any())).thenThrow(new APIClientException());

        assertThatThrownBy(() -> maatCourtDataService.outcomeCount(
                TestModelDataBuilder.TEST_REP_ID, TestModelDataBuilder.MEANS_ASSESSMENT_TRANSACTION_ID)
        ).isInstanceOf(APIClientException.class);
    }
}