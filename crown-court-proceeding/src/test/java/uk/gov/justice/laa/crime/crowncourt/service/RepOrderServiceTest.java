package uk.gov.justice.laa.crime.crowncourt.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiIOJAppeal;
import uk.gov.justice.laa.crime.crowncourt.model.ApiPassportAssessment;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class RepOrderServiceTest {

    @InjectMocks
    private RepOrderService repOrderService;

    @Test
    void givenValidIoJResult_whenGetReviewResultIsInvoked_ReviewResultIsReturned() {
        ApiIOJAppeal apiIOJAppeal = new ApiIOJAppeal().withIojResult(ReviewResult.PASS.getResult());
        assertThat(repOrderService.getReviewResult(apiIOJAppeal)).isEqualTo(ReviewResult.PASS);
    }

    @Test
    void givenNullIoJResultAndValidDecisionResult_whenGetReviewResultIsInvoked_ReviewResultIsReturned() {
        ApiIOJAppeal apiIOJAppeal = new ApiIOJAppeal().withDecisionResult(ReviewResult.FAIL.getResult());
        assertThat(repOrderService.getReviewResult(apiIOJAppeal)).isEqualTo(ReviewResult.FAIL);
    }

    @Test
    void givenNullIoJResultAndNullDecisionResult_whenGetReviewResultIsInvoked_NullIsReturned() {
        ApiIOJAppeal apiIOJAppeal = new ApiIOJAppeal();
        assertThat(repOrderService.getReviewResult(apiIOJAppeal)).isNull();
    }

    @Test
    void testCaseTypeConditions() {
        assertThat(repOrderService.isValidCaseType(CaseType.INDICTABLE, null, null)).isTrue();
        assertThat(repOrderService.isValidCaseType(CaseType.CC_ALREADY, null, null)).isTrue();
        assertThat(repOrderService.isValidCaseType(CaseType.COMMITAL, null, null)).isTrue();
        assertThat(repOrderService.isValidCaseType(CaseType.EITHER_WAY, MagCourtOutcome.COMMITTED_FOR_TRIAL, null)).isTrue();
        assertThat(repOrderService.isValidCaseType(CaseType.EITHER_WAY, null, null)).isFalse();
        assertThat(repOrderService.isValidCaseType(CaseType.APPEAL_CC, null, ReviewResult.PASS)).isTrue();
        assertThat(repOrderService.isValidCaseType(CaseType.APPEAL_CC, null, ReviewResult.FAIL)).isFalse();
        assertThat(repOrderService.isValidCaseType(null, null, null)).isFalse();
    }

    @Test
    void givenPassportAssessmentIsFail_whenGetDecisionByPassportAssessmentIsInvoked_NullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.FAIL.getResult());
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsPassAndStatusIsInProgress_whenGetDecisionByPassportAssessmentIsInvoked_NullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.PASS.getResult())
                .withStatus(CurrentStatus.IN_PROGRESS);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsPassAndStatusIsCompleteAndCheckCaseTypeIsFalse_whenGetDecisionByPassportAssessmentIsInvoked_NullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.PASS.getResult())
                .withStatus(CurrentStatus.COMPLETE);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, false))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsPassAndStatusIsCompleteAndCheckCaseTypeIsTrue_whenGetDecisionByPassportAssessmentIsInvoked_DecisionIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.PASS.getResult())
                .withStatus(CurrentStatus.COMPLETE);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isEqualTo(RepOrderService.GRANTED_PASSPORTED);
    }

    @Test
    void givenPassportAssessmentIsTempAndStatusIsInProgress_whenGetDecisionByPassportAssessmentIsInvoked_NullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.TEMP.getResult())
                .withStatus(CurrentStatus.IN_PROGRESS);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsTempAndStatusIsCompleteAndCheckCaseTypeIsFalse_whenGetDecisionByPassportAssessmentIsInvoked_NullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.TEMP.getResult())
                .withStatus(CurrentStatus.COMPLETE);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, false))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsTempAndStatusIsCompleteAndCheckCaseTypeIsTrue_whenGetDecisionByPassportAssessmentIsInvoked_DecisionIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.TEMP.getResult())
                .withStatus(CurrentStatus.COMPLETE);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isEqualTo(RepOrderService.GRANTED_PASSPORTED);
    }

    @Test
    void testDecisionReasonsByCaseType() {
        assertThat(repOrderService.getDecisionByCaseType(null, CaseType.SUMMARY_ONLY, null))
                .isNull();
        assertThat(repOrderService.getDecisionByCaseType(null, CaseType.COMMITAL, null))
                .isEqualTo(RepOrderService.FAILED_CF_S_FAILED_MEANS_TEST);
        assertThat(repOrderService.getDecisionByCaseType(null, CaseType.INDICTABLE, null))
                .isEqualTo(RepOrderService.GRANTED_FAILED_MEANS_TEST);
        assertThat(repOrderService.getDecisionByCaseType(null, CaseType.CC_ALREADY, null))
                .isEqualTo(RepOrderService.GRANTED_FAILED_MEANS_TEST);
        assertThat(repOrderService.getDecisionByCaseType(null, CaseType.EITHER_WAY, null))
                .isNull();
        assertThat(repOrderService.getDecisionByCaseType(null, CaseType.EITHER_WAY, MagCourtOutcome.COMMITTED_FOR_TRIAL))
                .isEqualTo(RepOrderService.GRANTED_FAILED_MEANS_TEST);
        assertThat(repOrderService.getDecisionByCaseType(ReviewResult.FAIL, CaseType.APPEAL_CC, null))
                .isNull();
        assertThat(repOrderService.getDecisionByCaseType(ReviewResult.PASS, CaseType.APPEAL_CC, null))
                .isEqualTo(RepOrderService.GRANTED_FAILED_MEANS_TEST);
    }

    @Test
    void givenCaseTypeIsAppealCCAndIOJDecisionFail_whenGetRepDecisionIsInvoked_validResponseIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getIojAppeal().setDecisionResult(ReviewResult.FAIL.getResult());
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isEqualTo(RepOrderService.FAILED_IO_J_APPEAL_FAILURE);
    }

    @Test
    void givenPrevDecisionMatchesNewDecision_whenGetRepDecisionIsInvoked_RepDateIsMatched() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getIojAppeal().setDecisionResult(ReviewResult.FAIL.getResult());
        requestDTO.getCrownCourtSummary().setRepOrderDecision(RepOrderService.FAILED_IO_J_APPEAL_FAILURE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isEqualTo(RepOrderService.FAILED_IO_J_APPEAL_FAILURE);
        assertThat(apiCrownCourtSummary.getRepOrderDate()).isEqualTo(requestDTO.getCrownCourtSummary().getRepOrderDate());
    }

    @Test
    void givenIndictableCaseWithPassportAssessmentIsTempAndStatusIsComplete_whenGetRepDecisionIsInvoked_DecisionIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.INDICTABLE);
        requestDTO.getIojAppeal().setDecisionResult(ReviewResult.FAIL.getResult());
        requestDTO.getPassportAssessment().setResult(PassportAssessmentResult.TEMP.getResult());
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isEqualTo(RepOrderService.GRANTED_PASSPORTED);
    }

    @Test
    void givenIneligibleFullAssessmentAndSentForTrail_whenGetRepDecisionIsInvoked_RefusedIneligibleIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.PASS);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.SENT_FOR_TRIAL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isEqualTo(RepOrderService.REFUSED_INELIGIBLE);
    }

    @Test
    void givenIneligibleFullAssessmentAndCommittedForTrail_whenGetRepDecisionIsInvoked_RefusedIneligibleIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.PASS);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isEqualTo(RepOrderService.REFUSED_INELIGIBLE);
    }

    @Test
    void givenAppealCCIneligibleInProgressFullAssessment_whenGetRepDecisionIsInvoked_GrantedPassedMeansTestIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.PASS);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isEqualTo(RepOrderService.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenIndictableCaseWithPassedHardshipOverview_whenGetRepDecisionIsInvoked_GrantedPassedMeansTestIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.PASS);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isEqualTo(RepOrderService.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenIndictableCaseWithFailedInitialAssessment_whenGetRepDecisionIsInvoked_GrantedFailedMeansTestIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isEqualTo(RepOrderService.GRANTED_FAILED_MEANS_TEST);
    }

    @Test
    void givenIndictableCaseWithFailedFullAssessment_whenGetRepDecisionIsInvoked_GrantedFailedMeansTestIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isEqualTo(RepOrderService.GRANTED_FAILED_MEANS_TEST);
    }

    @Test
    void givenInProgressFullAssessment_whenGetRepDecisionIsInvoked_NullDecisionIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision()).isNull();
    }

    private void setUpFinAssessment(CrownCourtsActionsRequestDTO requestDTO,
                                    CurrentStatus initStatus,
                                    CurrentStatus fullStatus,
                                    String initResult,
                                    String fullResult,
                                    ReviewResult hardshipReviewResult) {
        requestDTO.getFinancialAssessment().setInitStatus(initStatus);
        requestDTO.getFinancialAssessment().setFullStatus(fullStatus);
        requestDTO.getFinancialAssessment().setInitResult(initResult);
        requestDTO.getFinancialAssessment().setFullResult(fullResult);
        requestDTO.getFinancialAssessment().getHardshipOverview().setReviewResult(hardshipReviewResult);
    }

    @Test
    void givenInvalidCaseType_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false)).isNull();
    }

    @Test
    void givenIneligibleFullAssessmentWithCommittedOutcome_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.FAIL);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false)).isNull();
    }

    @Test
    void givenInProgressFullAssessmentFail_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false)).isNull();
    }

    @Test
    void givenInProgressInitAssessmentFail_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.IN_PROGRESS, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false)).isNull();
    }

    @Test
    void givenCommittalCaseWithFullAssessmentResultFail_whenGetDecisionByFinAssessmentIsInvoked_FailedCFSFailedMeansTestIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.IN_PROGRESS, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        requestDTO.setCaseType(CaseType.COMMITAL);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isEqualTo(RepOrderService.FAILED_CF_S_FAILED_MEANS_TEST);
    }

    @Test
    void givenHardshipOverviewResultPassWithValidCaseType_whenGetDecisionByFinAssessmentIsInvoked_GrantedPassedMeansTestIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.IN_PROGRESS, CurrentStatus.COMPLETE,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.PASS);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true))
                .isEqualTo(RepOrderService.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenHardshipOverviewResultPassWithFullAssessmentInProgress_whenGetDecisionByFinAssessmentIsInvoked_GrantedPassedMeansTestIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.PASS.getResult(), ReviewResult.PASS);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true))
                .isEqualTo(RepOrderService.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenHardshipOverviewInProgress_whenGetDecisionByFinAssessmentIsInvoked_NullIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.PASS.getResult(), ReviewResult.PASS);
        requestDTO.getFinancialAssessment().getHardshipOverview().setAssessmentStatus(CurrentStatus.IN_PROGRESS);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true)).isNull();
    }
}
