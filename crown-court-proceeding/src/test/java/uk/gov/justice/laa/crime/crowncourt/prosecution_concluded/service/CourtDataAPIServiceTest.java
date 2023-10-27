package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

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
import uk.gov.justice.laa.crime.crowncourt.config.MockServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQLinkRegisterDTO;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateSentenceOrder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class CourtDataAPIServiceTest {

    @Mock
    RestAPIClient maatAPIClient;

    @InjectMocks
    private CourtDataAPIService courtDataAPIService;

    @Spy
    private ServicesConfiguration configuration = MockServicesConfiguration.getConfiguration(1000);

    private static final String LAA_TRANSACTION_ID = "laaTransactionId";

    @Test
    void givenAInvalidParameter_whenRetrieveHearingForCaseConclusionIsInvoked_thenEmptyIsReturned() {
        WQHearingDTO wqHearingDTO = courtDataAPIService.retrieveHearingForCaseConclusion(
                ProsecutionConcluded.builder().hearingIdWhereChangeOccurred(UUID.randomUUID())
                        .maatId(TestModelDataBuilder.TEST_REP_ID).build()
        );
        assertThat(wqHearingDTO).isNull();
    }

    @Test
    void givenAInvalidParameter_whenFindWQLinkRegisterByMaatIdIsInvoked_thenNullIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any()))
                .thenReturn(null);
        int caseId = courtDataAPIService.findWQLinkRegisterByMaatId(TestModelDataBuilder.TEST_REP_ID);
        assertThat(caseId).isZero();
    }

    @Test
    void givenAInvalidParameter_whenFindWQLinkRegisterByMaatIdIsInvoked_thenEmptyIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any()))
                .thenReturn(List.of());
        int caseId = courtDataAPIService.findWQLinkRegisterByMaatId(TestModelDataBuilder.TEST_REP_ID);
        assertThat(caseId).isZero();
    }

    @Test
    void givenAValidParameter_whenFindWQLinkRegisterByMaatIdIsInvoked_thenResponseIsReturned() {
        when(maatAPIClient.get(any(), any(), any(), any()))
                .thenReturn(
                        List.of(WQLinkRegisterDTO.builder()
                                .caseId(TestModelDataBuilder.TEST_CASE_ID)
                                .build()
                        )
                );
        int caseId = courtDataAPIService.findWQLinkRegisterByMaatId(TestModelDataBuilder.TEST_REP_ID);
        assertThat(caseId).isEqualTo(TestModelDataBuilder.TEST_CASE_ID);
    }

    @Test
    void givenAValidParameter_whenFindOffenceByCaseIdIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.findOffenceByCaseId(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient).get(any(), any(), any(), any());
    }

    @Test
    void givenAInvalidParameter_whenGetOffenceNewOffenceCountIsInvoked_thenNullIsReturned() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(0);
        ResponseEntity<Void> expected = ResponseEntity.ok().headers(responseHeaders).build();
        when(maatAPIClient.head(any(), anyMap(), any(), any()))
                .thenReturn(expected);
        long offenceCount = courtDataAPIService.getOffenceNewOffenceCount(
                TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID
        );
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAInvalidParameter_whenGetOffenceNewOffenceCountIsInvoked_thenEmptyIsReturned() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(0);
        ResponseEntity<Void> expected = ResponseEntity.ok().headers(responseHeaders).build();
        when(maatAPIClient.head(any(), anyMap(), any(), any()))
                .thenReturn(expected);
        long offenceCount = courtDataAPIService.getOffenceNewOffenceCount(
                TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID
        );
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAValidParameter_whenGetOffenceNewOffenceCountIsInvoked_thenResponseIsReturned() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(4);
        ResponseEntity<Void> expected = ResponseEntity.ok().headers(responseHeaders).build();
        when(maatAPIClient.head(any(), anyMap(), any(), any()))
                .thenReturn(expected);
        long offenceCount = courtDataAPIService.getOffenceNewOffenceCount(
                TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID
        );
        assertThat(offenceCount).isEqualTo(4);
    }

    @Test
    void givenAInvalidParameter_whenGetWQOffenceNewOffenceCountIsInvoked_thenNullIsReturned() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(0);
        ResponseEntity<Void> expected = ResponseEntity.ok().headers(responseHeaders).build();
        when(maatAPIClient.head(any(), anyMap(), any(), any()))
                .thenReturn(expected);
        long offenceCount = courtDataAPIService.getWQOffenceNewOffenceCount(
                TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID
        );
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAInvalidParameter_whenGetWQOffenceNewOffenceCountIsInvoked_thenEmptyIsReturned() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(0);
        ResponseEntity<Void> expected = ResponseEntity.ok().headers(responseHeaders).build();
        when(maatAPIClient.head(any(), anyMap(), any(), any()))
                .thenReturn(expected);
        long offenceCount = courtDataAPIService.getWQOffenceNewOffenceCount(
                TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID
        );
        assertThat(offenceCount).isZero();
    }

    @Test
    void givenAValidParameter_whenGetWQOffenceNewOffenceCountIsInvoked_thenResponseIsReturned() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(5);
        ResponseEntity<Void> expected = ResponseEntity.ok().headers(responseHeaders).build();
        when(maatAPIClient.head(any(), anyMap(), any(), any()))
                .thenReturn(expected);
        long offenceCount = courtDataAPIService.getWQOffenceNewOffenceCount(
                TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID
        );
        assertThat(offenceCount).isEqualTo(5);
    }

    @Test
    void givenAValidParameter_whenFindResultsByWQTypeSubTypeIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.findResultsByWQTypeSubType(1244, 25243);
        verify(maatAPIClient).get(any(), anyString(), anyMap(), anyInt(), anyInt());
    }

    @Test
    void givenAValidParameter_whenGetResultCodeByCaseIdAndAsnSeqIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.getResultCodeByCaseIdAndAsnSeq(
                TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID
        );
        verify(maatAPIClient).get(any(), anyString(), anyMap(), anyInt(), anyString());
    }

    @Test
    void givenAInvalidParameter_whenGetWqResultCodeByCaseIdAndAsnSeqIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.getWqResultCodeByCaseIdAndAsnSeq(
                TestModelDataBuilder.TEST_CASE_ID, TestModelDataBuilder.TEST_OFFENCE_ID
        );
        verify(maatAPIClient).get(any(), anyString(), anyMap(), anyInt(), anyString());
    }

    @Test
    void givenAValidParameter_whenFetchResultCodesForCCImprisonmentIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.fetchResultCodesForCCImprisonment();
        verify(maatAPIClient).get(any(), anyString(), anyMap());
    }

    @Test
    void givenAValidParameter_whenFindByCjsResultCodeInIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.findByCjsResultCodeIn();
        verify(maatAPIClient).get(any(), anyString(), anyMap());
    }

    @Test
    void givenAValidParameter_whenGetRepOrderIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.getRepOrder(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient).get(any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenUpdateCrownCourtOutcomeIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.updateCrownCourtOutcome(UpdateCCOutcome.builder().build());
        verify(maatAPIClient).put(any(), any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenInvokeUpdateAppealSentenceOrderDateIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.invokeUpdateAppealSentenceOrderDate(UpdateSentenceOrder.builder().build());
        verify(maatAPIClient).get(any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenInvokeUpdateSentenceOrderDateIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.invokeUpdateSentenceOrderDate(UpdateSentenceOrder.builder().build());
        verify(maatAPIClient).get(any(), any(), any());
    }

    @Test
    void givenAValidParameter_whenMaatRecordLockedIsInvoked_thenResponseIsReturned() {
        courtDataAPIService.isMaatRecordLocked(TestModelDataBuilder.TEST_REP_ID);
        verify(maatAPIClient).get(any(), any(), any());
    }
}
