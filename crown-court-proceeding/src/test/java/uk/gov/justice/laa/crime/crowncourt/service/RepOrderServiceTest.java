package uk.gov.justice.laa.crime.crowncourt.service;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiIOJAppeal;
import uk.gov.justice.laa.crime.crowncourt.model.ApiPassportAssessment;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class RepOrderServiceTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @InjectMocks
    private RepOrderService repOrderService;

    @Mock
    private MaatCourtDataService maatCourtDataService;

    @Test
    void givenValidIoJResult_whenGetReviewResultIsInvoked_reviewResultIsReturned() {
        ApiIOJAppeal apiIOJAppeal = new ApiIOJAppeal().withIojResult(ReviewResult.PASS.getResult());
        assertThat(repOrderService.getReviewResult(apiIOJAppeal))
                .isEqualTo(ReviewResult.PASS);
    }

    @Test
    void givenNullIoJResultAndValidDecisionResult_whenGetReviewResultIsInvoked_reviewResultIsReturned() {
        ApiIOJAppeal apiIOJAppeal = new ApiIOJAppeal().withDecisionResult(ReviewResult.FAIL.getResult());
        assertThat(repOrderService.getReviewResult(apiIOJAppeal))
                .isEqualTo(ReviewResult.FAIL);
    }

    @Test
    void givenNullIoJResultAndNullDecisionResult_whenGetReviewResultIsInvoked_nullIsReturned() {
        ApiIOJAppeal apiIOJAppeal = new ApiIOJAppeal();
        assertThat(repOrderService.getReviewResult(apiIOJAppeal))
                .isNull();
    }

    @Test
    void testCaseTypeConditions() {
        softly.assertThat(repOrderService.isValidCaseType(CaseType.INDICTABLE, null, null))
                .isTrue();

        softly.assertThat(repOrderService.isValidCaseType(CaseType.CC_ALREADY, null, null))
                .isTrue();

        softly.assertThat(repOrderService.isValidCaseType(CaseType.COMMITAL, null, null))
                .isTrue();

        softly.assertThat(repOrderService.isValidCaseType(CaseType.EITHER_WAY, MagCourtOutcome.COMMITTED_FOR_TRIAL, null))
                .isTrue();

        softly.assertThat(repOrderService.isValidCaseType(CaseType.EITHER_WAY, null, null))
                .isFalse();

        softly.assertThat(repOrderService.isValidCaseType(CaseType.APPEAL_CC, null, ReviewResult.PASS))
                .isTrue();

        softly.assertThat(repOrderService.isValidCaseType(CaseType.APPEAL_CC, null, ReviewResult.FAIL))
                .isFalse();

        softly.assertThat(repOrderService.isValidCaseType(null, null, null))
                .isFalse();
    }

    @Test
    void givenPassportAssessmentIsFail_whenGetDecisionByPassportAssessmentIsInvoked_nullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.FAIL.getResult());
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsPassAndStatusIsInProgress_whenGetDecisionByPassportAssessmentIsInvoked_nullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.PASS.getResult())
                .withStatus(CurrentStatus.IN_PROGRESS);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsPassAndStatusIsCompleteAndCheckCaseTypeIsFalse_whenGetDecisionByPassportAssessmentIsInvoked_nullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.PASS.getResult())
                .withStatus(CurrentStatus.COMPLETE);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, false))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsPassAndStatusIsCompleteAndCheckCaseTypeIsTrue_whenGetDecisionByPassportAssessmentIsInvoked_decisionIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.PASS.getResult())
                .withStatus(CurrentStatus.COMPLETE);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isEqualTo(Constants.GRANTED_PASSPORTED);
    }

    @Test
    void givenPassportAssessmentIsTempAndStatusIsInProgress_whenGetDecisionByPassportAssessmentIsInvoked_nullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.TEMP.getResult())
                .withStatus(CurrentStatus.IN_PROGRESS);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsTempAndStatusIsCompleteAndCheckCaseTypeIsFalse_whenGetDecisionByPassportAssessmentIsInvoked_nullIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.TEMP.getResult())
                .withStatus(CurrentStatus.COMPLETE);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, false))
                .isNull();
    }

    @Test
    void givenPassportAssessmentIsTempAndStatusIsCompleteAndCheckCaseTypeIsTrue_whenGetDecisionByPassportAssessmentIsInvoked_decisionIsReturned() {
        ApiPassportAssessment apiPassportAssessment = new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.TEMP.getResult())
                .withStatus(CurrentStatus.COMPLETE);
        assertThat(repOrderService.getDecisionByPassportAssessment(apiPassportAssessment, true))
                .isEqualTo(Constants.GRANTED_PASSPORTED);
    }

    @Test
    void testDecisionReasonsByCaseType() {
        softly.assertThat(repOrderService.getDecisionByCaseType(null, CaseType.SUMMARY_ONLY, null))
                .isNull();

        softly.assertThat(repOrderService.getDecisionByCaseType(null, CaseType.COMMITAL, null))
                .isEqualTo(Constants.FAILED_CF_S_FAILED_MEANS_TEST);

        softly.assertThat(repOrderService.getDecisionByCaseType(null, CaseType.INDICTABLE, null))
                .isEqualTo(Constants.GRANTED_FAILED_MEANS_TEST);

        softly.assertThat(repOrderService.getDecisionByCaseType(null, CaseType.CC_ALREADY, null))
                .isEqualTo(Constants.GRANTED_FAILED_MEANS_TEST);

        softly.assertThat(repOrderService.getDecisionByCaseType(null, CaseType.EITHER_WAY, null))
                .isNull();

        softly.assertThat(repOrderService.getDecisionByCaseType(null, CaseType.EITHER_WAY, MagCourtOutcome.COMMITTED_FOR_TRIAL))
                .isEqualTo(Constants.GRANTED_FAILED_MEANS_TEST);

        softly.assertThat(repOrderService.getDecisionByCaseType(ReviewResult.FAIL, CaseType.APPEAL_CC, null))
                .isNull();

        softly.assertThat(repOrderService.getDecisionByCaseType(ReviewResult.PASS, CaseType.APPEAL_CC, null))
                .isEqualTo(Constants.GRANTED_FAILED_MEANS_TEST);
    }

    @Test
    void givenCaseTypeIsAppealCCAndIOJDecisionFail_whenGetRepDecisionIsInvoked_validResponseIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getIojAppeal().setDecisionResult(ReviewResult.FAIL.getResult());
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.FAILED_IO_J_APPEAL_FAILURE);
    }

    @Test
    void givenPrevDecisionMatchesNewDecision_whenGetRepDecisionIsInvoked_repDateIsMatched() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getIojAppeal().setDecisionResult(ReviewResult.FAIL.getResult());
        requestDTO.getCrownCourtSummary().setRepOrderDecision(Constants.FAILED_IO_J_APPEAL_FAILURE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);

        softly.assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.FAILED_IO_J_APPEAL_FAILURE);

        softly.assertThat(apiCrownCourtSummary.getRepOrderDate())
                .isEqualTo(requestDTO.getCrownCourtSummary().getRepOrderDate());
    }

    @Test
    void givenIndictableCaseWithPassportAssessmentIsTempAndStatusIsComplete_whenGetRepDecisionIsInvoked_decisionIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.INDICTABLE);
        requestDTO.getIojAppeal().setDecisionResult(ReviewResult.FAIL.getResult());
        requestDTO.getPassportAssessment().setResult(PassportAssessmentResult.TEMP.getResult());
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.GRANTED_PASSPORTED);
    }

    @Test
    void givenIneligibleFullAssessmentAndSentForTrail_whenGetRepDecisionIsInvoked_refusedIneligibleIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.PASS);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.SENT_FOR_TRIAL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.REFUSED_INELIGIBLE);
    }

    @Test
    void givenIneligibleFullAssessmentAndCommittedForTrail_whenGetRepDecisionIsInvoked_refusedIneligibleIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.PASS);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.REFUSED_INELIGIBLE);
    }

    @Test
    void givenAppealCCIneligibleInProgressFullAssessment_whenGetRepDecisionIsInvoked_grantedPassedMeansTestIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.PASS);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenIndictableCaseWithPassedHardshipOverview_whenGetRepDecisionIsInvoked_grantedPassedMeansTestIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.PASS);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenIndictableCaseWithFailedInitialAssessment_whenGetRepDecisionIsInvoked_grantedFailedMeansTestIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.GRANTED_FAILED_MEANS_TEST);
    }

    @Test
    void givenIndictableCaseWithFailedFullAssessment_whenGetRepDecisionIsInvoked_grantedFailedMeansTestIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.GRANTED_FAILED_MEANS_TEST);
    }

    @Test
    void givenInProgressFullAssessment_whenGetRepDecisionIsInvoked_nullDecisionIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isNull();
    }

    private void setUpFinAssessment(CrownCourtActionsRequestDTO requestDTO,
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
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isNull();
    }

    @Test
    void givenIneligibleFullAssessmentWithCommittedOutcome_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.FAIL);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isNull();
    }

    @Test
    void givenInProgressFullAssessmentFail_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isNull();
    }

    @Test
    void givenInProgressInitAssessmentFail_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.IN_PROGRESS, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isNull();
    }

    @Test
    void givenCommittalCaseWithFullAssessmentResultFail_whenGetDecisionByFinAssessmentIsInvoked_failedCFSFailedMeansTestIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.IN_PROGRESS, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        requestDTO.setCaseType(CaseType.COMMITAL);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isEqualTo(Constants.FAILED_CF_S_FAILED_MEANS_TEST);
    }

    @Test
    void givenHardshipOverviewResultPassWithValidCaseType_whenGetDecisionByFinAssessmentIsInvoked_grantedPassedMeansTestIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.IN_PROGRESS, CurrentStatus.COMPLETE,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.PASS);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true))
                .isEqualTo(Constants.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenHardshipOverviewResultPassWithFullAssessmentInProgress_whenGetDecisionByFinAssessmentIsInvoked_grantedPassedMeansTestIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.PASS.getResult(), ReviewResult.PASS);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true))
                .isEqualTo(Constants.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenHardshipOverviewInProgress_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.PASS.getResult(), ReviewResult.PASS);
        requestDTO.getFinancialAssessment().getHardshipOverview().setAssessmentStatus(CurrentStatus.IN_PROGRESS);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true))
                .isNull();
    }

    @Test
    void givenGrantedDecisionReason_whenDetermineRepTypeByDecisionReasonIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setDecisionReason(DecisionReason.GRANTED);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);

        softly.assertThat(apiCrownCourtSummary.getRepId())
                .isEqualTo(requestDTO.getRepId());

        softly.assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.THROUGH_ORDER);
    }

    @Test
    void givenDecisionReasonIsFailIoJ_whenDetermineRepTypeByDecisionReasonIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setDecisionReason(DecisionReason.FAILIOJ);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenDecisionReasonIsFailMeans_whenDetermineRepTypeByDecisionReasonIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setDecisionReason(DecisionReason.FAILMEANS);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenDecisionReasonIsFailMEIoJ_whenDetermineRepTypeByDecisionReasonIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setDecisionReason(DecisionReason.FAILMEIOJ);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenDecisionReasonIsAbandoned_whenDetermineRepTypeByDecisionReasonIsInvoked_blankRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setDecisionReason(DecisionReason.ABANDONED);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isBlank();
    }

    @Test
    void givenFailedCFSMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.FAILED_CF_S_FAILED_MEANS_TEST);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.NOT_ELIGIBLE_FOR_REP_ORDER);
    }

    @Test
    void givenFailedIoJAppealFailure_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.FAILED_IO_J_APPEAL_FAILURE);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.NOT_ELIGIBLE_FOR_REP_ORDER);
    }

    @Test
    void givenAppealCCCaseTypeWithGrantedFailedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_FAILED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenCommittalCaseTypeWithGrantedFailedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_repTypeIsBlank() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_FAILED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.COMMITAL);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isBlank();
    }

    @Test
    void givenAppealCCCaseTypeWithGrantedPassported_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSPORTED);
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenAppealCCCaseTypeWithGrantedPassedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenCommittalCaseTypeWithGrantedPassported_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSPORTED);
        requestDTO.setCaseType(CaseType.COMMITAL);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenCommittalCaseTypeWithGrantedPassedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.COMMITAL);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenIndictableCaseTypeWithGrantedPassported_whenDetermineRepTypeByRepOrderDecisionIsInvoked_RepTypeIsBlank() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSPORTED);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isBlank();
    }

    @Test
    void givenIndictableCaseTypeWithGrantedPassedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_RepTypeIsBlank() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isBlank();
    }

    @Test
    void givenRepOrderDecisionIsInel_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.REFUSED_INELIGIBLE);
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenRepOrderDecisionIsNull_whenDetermineCrownRepTypeIsInvoked_BlankRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.getCrownCourtSummary().setRepOrderDecision(null);
        assertThat(repOrderService.determineCrownRepType(requestDTO).getRepType())
                .isBlank();
    }

    @Test
    void givenSentForTrailWithGranted_whenDetermineCrownRepTypeIsInvoked_ValidRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setMagCourtOutcome(MagCourtOutcome.SENT_FOR_TRIAL);
        requestDTO.setDecisionReason(DecisionReason.GRANTED);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.determineCrownRepType(requestDTO);

        softly.assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.THROUGH_ORDER);

        softly.assertThat(apiCrownCourtSummary.getRepId())
                .isEqualTo(requestDTO.getRepId());
    }

    @Test
    void givenCommittedForTrailWithGranted_whenDetermineCrownRepTypeIsInvoked_ValidRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setDecisionReason(DecisionReason.GRANTED);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.determineCrownRepType(requestDTO);

        softly.assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.THROUGH_ORDER);

        softly. assertThat(apiCrownCourtSummary.getRepId())
                .isEqualTo(requestDTO.getRepId());
    }

    @Test
    void givenCCAlreadyCaseType_whenDetermineCrownRepTypeIsInvoked_ValidRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.CC_ALREADY);
        assertThat(repOrderService.determineCrownRepType(requestDTO).getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenValidWithdrawalDate_whenDetermineCrownRepTypeIsInvoked_ValidRepTypeIsReturned() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.getCrownCourtSummary().setWithdrawalDate(TestModelDataBuilder.TEST_WITHDRAWAL_DATE);
        assertThat(repOrderService.determineCrownRepType(requestDTO).getRepType())
                .isEqualTo(Constants.DECLINED_REP_ORDER);
    }

    @Test
    void givenRepOrderDecisionIsNull_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsNotChanged() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.getCrownCourtSummary().setRepOrderDecision(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getCrownCourtSummary().getRepOrderDate());
    }

    @Test
    void givenRepOrderDateIsNotNull_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsNotChanged() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getCrownCourtSummary().getRepOrderDate());
    }

    @Test
    void givenIndictableCase_whenDetermineRepOrderDateIsInvoked_DecisionDateIsSetAsRepOrderDate() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.INDICTABLE);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getDecisionDate());
    }

    @Test
    void givenCommittedEitherWayCase_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsNull() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isNull();
    }
    @Test
    void givenEitherWayCase_whenDetermineRepOrderDateIsInvoked_thenRepOrderDateIsCorrect() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);

        requestDTO.setDecisionReason(DecisionReason.GRANTED);
        softly.assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getDecisionDate());

        requestDTO.setDecisionReason(DecisionReason.FAILIOJ);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        softly.assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getCommittalDate());

        requestDTO.setDecisionReason(DecisionReason.FAILMEIOJ);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        softly.assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getCommittalDate());

        requestDTO.setDecisionReason(DecisionReason.FAILMEANS);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        softly.assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getCommittalDate());

        requestDTO.setDecisionReason(DecisionReason.ABANDONED);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        softly.assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isNull();

        requestDTO.getCrownCourtSummary().setRepOrderDecision(Constants.REFUSED_INELIGIBLE);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        softly.assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getCommittalDate());
    }

    @Test
    void givenCommittalCase_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsSetToDateReceived() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.COMMITAL);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getDateReceived());
    }

    @Test
    void givenCCAlreadyCase_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsSetToDateReceived() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.CC_ALREADY);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getDateReceived());
    }

    @Test
    void givenSummaryOnlyCase_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsNull() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isNull();
    }

    @Test
    void givenAppealCCCaseWithExistingIOJAppeal_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsSetToIOJAppealDecisionDate() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        when(maatCourtDataService.getCurrentPassedIOJAppealFromRepId(requestDTO.getRepId(), requestDTO.getLaaTransactionId()))
                .thenReturn(TestModelDataBuilder.getIOJAppealDTO());
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_IOJ_APPEAL_DECISION_DATE);
    }

    @Test
    void givenAppealCCCaseWithNoIOJAppeal_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsSetToDateReceived() {
        CrownCourtActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        when(maatCourtDataService.getCurrentPassedIOJAppealFromRepId(requestDTO.getRepId(), requestDTO.getLaaTransactionId()))
                .thenReturn(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_DATE_RECEIVED);
    }

    @Test
    void givenUpdateRepOrderRequest_whenUpdateCCSentenceOrderDateIsInvoked_thenUpdateRepOrderIsPerformed() {
        repOrderService.updateCCSentenceOrderDate(TestModelDataBuilder.getCrownCourtApplicationRequestDTO());
        verify(maatCourtDataService).updateRepOrder(any(UpdateRepOrderRequestDTO.class), anyString());
    }
}
