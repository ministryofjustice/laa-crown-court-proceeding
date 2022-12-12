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
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtApplicationRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsResponse;
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
        when(repOrderService.getRepDecision(any(CrownCourtActionsRequestDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
        when(repOrderService.determineCrownRepType(any(CrownCourtActionsRequestDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
        when(repOrderService.determineRepOrderDate(any(CrownCourtActionsRequestDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
    }

    @Test
    void givenValidIndictableCase_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.INDICTABLE);
        setupMockData();
        ApiCheckCrownCourtActionsResponse response = crownCourtProceedingService.checkCrownCourtActions(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidCCAlreadyCase_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.CC_ALREADY);
        setupMockData();
        ApiCheckCrownCourtActionsResponse response = crownCourtProceedingService.checkCrownCourtActions(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidAppealCCCase_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        setupMockData();
        ApiCheckCrownCourtActionsResponse response = crownCourtProceedingService.checkCrownCourtActions(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidCommittalCase_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.COMMITAL);
        setupMockData();
        ApiCheckCrownCourtActionsResponse response = crownCourtProceedingService.checkCrownCourtActions(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithCommittedMagOutcome_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED);
        setupMockData();
        ApiCheckCrownCourtActionsResponse response = crownCourtProceedingService.checkCrownCourtActions(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithCommittedForTrailMagOutcome_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        setupMockData();
        ApiCheckCrownCourtActionsResponse response = crownCourtProceedingService.checkCrownCourtActions(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithSentForTrailMagOutcome_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.SENT_FOR_TRIAL);
        setupMockData();
        ApiCheckCrownCourtActionsResponse response = crownCourtProceedingService.checkCrownCourtActions(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithAppealToCCMagOutcome_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.APPEAL_TO_CC);
        setupMockData();
        ApiCheckCrownCourtActionsResponse response = crownCourtProceedingService.checkCrownCourtActions(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenSummaryOnlyCase_whenCheckCrownCourtActionsIsInvoked_emptyResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        assertThat(crownCourtProceedingService.checkCrownCourtActions(requestDTO))
                .isEqualTo(new ApiCheckCrownCourtActionsResponse());
    }

    @Test
    void givenEitherWayCaseWithResolvedMagOutcome_whenCheckCrownCourtActionsIsInvoked_emptyResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.RESOLVED_IN_MAGS);
        assertThat(crownCourtProceedingService.checkCrownCourtActions(requestDTO))
                .isEqualTo(new ApiCheckCrownCourtActionsResponse());
    }

    @Test
    void givenCCApplication_whenUpdateCrownCourtApplicationIsInvoked_thenSentenceOrderDateIsPersisted() {
        CrownCourtApplicationRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtApplicationRequestDTO();
        crownCourtProceedingService.updateCrownCourtApplication(requestDTO);
        verify(repOrderService).updateCCSentenceOrderDate(any(CrownCourtApplicationRequestDTO.class));
    }


}
