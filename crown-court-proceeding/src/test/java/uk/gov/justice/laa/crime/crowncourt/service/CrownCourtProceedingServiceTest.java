package uk.gov.justice.laa.crime.crowncourt.service;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.ProcessCrownRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtApplicationRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessCrownRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MagCourtOutcome;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class CrownCourtProceedingServiceTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @InjectMocks
    private CrownCourtProceedingService crownCourtProceedingService;

    @Mock
    private RepOrderService repOrderService;

    private void setupMockData() {
        when(repOrderService.getRepDecision(any(ProcessCrownRepOrderRequestDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
        when(repOrderService.determineCrownRepType(any(ProcessCrownRepOrderRequestDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
        when(repOrderService.determineRepOrderDate(any(ProcessCrownRepOrderRequestDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
    }

    @Test
    void givenValidIndictableCase_whenProcessCrownRepOrderIsInvoked_validResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        requestDTO.setCaseType(CaseType.INDICTABLE);
        setupMockData();
        ApiProcessCrownRepOrderResponse response = crownCourtProceedingService.processCrownRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidCCAlreadyCase_whenProcessCrownRepOrderIsInvoked_validResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        requestDTO.setCaseType(CaseType.CC_ALREADY);
        setupMockData();
        ApiProcessCrownRepOrderResponse response = crownCourtProceedingService.processCrownRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidAppealCCCase_whenProcessCrownRepOrderIsInvoked_validResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        setupMockData();
        ApiProcessCrownRepOrderResponse response = crownCourtProceedingService.processCrownRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidCommittalCase_whenProcessCrownRepOrderIsInvoked_validResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        requestDTO.setCaseType(CaseType.COMMITAL);
        setupMockData();
        ApiProcessCrownRepOrderResponse response = crownCourtProceedingService.processCrownRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithCommittedMagOutcome_whenProcessCrownRepOrderIsInvoked_validResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED);
        setupMockData();
        ApiProcessCrownRepOrderResponse response = crownCourtProceedingService.processCrownRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithCommittedForTrailMagOutcome_whenProcessCrownRepOrderIsInvoked_validResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        setupMockData();
        ApiProcessCrownRepOrderResponse response = crownCourtProceedingService.processCrownRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithSentForTrailMagOutcome_whenProcessCrownRepOrderIsInvoked_validResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.SENT_FOR_TRIAL);
        setupMockData();
        ApiProcessCrownRepOrderResponse response = crownCourtProceedingService.processCrownRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithAppealToCCMagOutcome_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.APPEAL_TO_CC);
        setupMockData();
        ApiProcessCrownRepOrderResponse response = crownCourtProceedingService.processCrownRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenSummaryOnlyCase_whenProcessCrownRepOrderIsInvoked_emptyResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        assertThat(crownCourtProceedingService.processCrownRepOrder(requestDTO))
                .isEqualTo(new ApiProcessCrownRepOrderResponse());
    }

    @Test
    void givenEitherWayCaseWithResolvedMagOutcome_whenProcessCrownRepOrderIsInvoked_emptyResponseIsReturned() {
        ProcessCrownRepOrderRequestDTO requestDTO = TestModelDataBuilder.getProcessCrownRepOrderRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.RESOLVED_IN_MAGS);
        assertThat(crownCourtProceedingService.processCrownRepOrder(requestDTO))
                .isEqualTo(new ApiProcessCrownRepOrderResponse());
    }

    @Test
    void givenCCApplication_whenUpdateCrownCourtApplicationIsInvoked_thenSentenceOrderDateIsPersisted() {
        CrownCourtApplicationRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtApplicationRequestDTO();
        crownCourtProceedingService.updateCrownCourtApplication(requestDTO);
        verify(repOrderService).updateCCSentenceOrderDate(any(CrownCourtApplicationRequestDTO.class));
    }


}
