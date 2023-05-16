package uk.gov.justice.laa.crime.crowncourt.service;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.commons.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCalculateEvidenceFeeResponse;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiIOJAppeal;
import uk.gov.justice.laa.crime.crowncourt.model.ApiPassportAssessment;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class RepOrderServiceTest {

    @InjectSoftAssertions
    private SoftAssertions softly;
    @InjectMocks
    private RepOrderService repOrderService;
    @Mock
    private MaatCourtDataService maatCourtDataService;

    @Mock
    private CrimeEvidenceDataService crimeEvidenceDataService;

    private static final String ERROR_MSG = "Call to Court Data API failed, invalid response";

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
        softly.assertAll();
    }

    @Test
    void givenPassportAssessmentIsNull_whenGetDecisionByPassportAssessmentIsInvoked_nullIsReturned() {
        assertThat(repOrderService.getDecisionByPassportAssessment(null, true))
                .isNull();
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
        softly.assertAll();
    }

    @Test
    void givenCaseTypeIsAppealCCAndIOJDecisionFail_whenGetRepDecisionIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getIojAppeal().setDecisionResult(ReviewResult.FAIL.getResult());
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.FAILED_IO_J_APPEAL_FAILURE);
    }

    @Test
    void givenPrevDecisionMatchesNewDecision_whenGetRepDecisionIsInvoked_repDateIsMatched() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getIojAppeal().setDecisionResult(ReviewResult.FAIL.getResult());
        requestDTO.getCrownCourtSummary().setRepOrderDecision(Constants.FAILED_IO_J_APPEAL_FAILURE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);

        softly.assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.FAILED_IO_J_APPEAL_FAILURE);

        softly.assertThat(apiCrownCourtSummary.getRepOrderDate())
                .isEqualTo(requestDTO.getCrownCourtSummary().getRepOrderDate());
        softly.assertAll();
    }

    @Test
    void givenIndictableCaseWithPassportAssessmentIsTempAndStatusIsComplete_whenGetRepDecisionIsInvoked_decisionIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.INDICTABLE);
        requestDTO.getIojAppeal().setDecisionResult(ReviewResult.FAIL.getResult());
        requestDTO.getPassportAssessment().setResult(PassportAssessmentResult.TEMP.getResult());
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.GRANTED_PASSPORTED);
    }

    @Test
    void givenIneligibleFullAssessmentAndSentForTrail_whenGetRepDecisionIsInvoked_refusedIneligibleIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.PASS);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.SENT_FOR_TRIAL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.REFUSED_INELIGIBLE);
    }

    @Test
    void givenIneligibleFullAssessmentAndCommittedForTrail_whenGetRepDecisionIsInvoked_refusedIneligibleIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.PASS);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.REFUSED_INELIGIBLE);
    }

    @Test
    void givenAppealCCIneligibleInProgressFullAssessment_whenGetRepDecisionIsInvoked_grantedPassedMeansTestIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
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
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.PASS);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenIndictableCaseWithFailedInitialAssessment_whenGetRepDecisionIsInvoked_grantedFailedMeansTestIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.GRANTED_FAILED_MEANS_TEST);
    }

    @Test
    void givenIndictableCaseWithFailedFullAssessment_whenGetRepDecisionIsInvoked_grantedFailedMeansTestIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isEqualTo(Constants.GRANTED_FAILED_MEANS_TEST);
    }

    @Test
    void givenInProgressFullAssessment_whenGetRepDecisionIsInvoked_nullDecisionIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        assertThat(apiCrownCourtSummary.getRepOrderDecision())
                .isNull();
    }

    private void setUpFinAssessment(CrownCourtDTO requestDTO,
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
    void givenInvalidCaseType_whenGetDecisionByFinAssessmentIsInvoked_ThenNullIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isNull();
    }

    @Test
    void givenIneligibleFullAssessmentWithCommittedOutcome_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.COMPLETE,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.INEL.getResult(), ReviewResult.FAIL);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isNull();
    }

    @Test
    void givenInProgressFullAssessmentFail_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isNull();
    }

    @Test
    void givenInProgressInitAssessmentFail_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.IN_PROGRESS, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isNull();
    }

    @Test
    void givenCommittalCaseWithFullAssessmentResultFail_whenGetDecisionByFinAssessmentIsInvoked_failedCFSFailedMeansTestIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.IN_PROGRESS, CurrentStatus.COMPLETE,
                InitAssessmentResult.FAIL.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.FAIL);
        requestDTO.setCaseType(CaseType.COMMITAL);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, false))
                .isEqualTo(Constants.FAILED_CF_S_FAILED_MEANS_TEST);
    }

    @Test
    void givenHardshipOverviewResultIsNull_whenGetDecisionByFinAssessmentIsInvoked_thenNullIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(
                requestDTO, CurrentStatus.COMPLETE, null,
                InitAssessmentResult.FULL.getResult(), null, null
        );
        requestDTO.getFinancialAssessment().setHardshipOverview(null);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true))
                .isNull();
    }

    @Test
    void givenHardshipOverviewResultPassWithValidCaseType_whenGetDecisionByFinAssessmentIsInvoked_grantedPassedMeansTestIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.IN_PROGRESS, CurrentStatus.COMPLETE,
                InitAssessmentResult.PASS.getResult(), FullAssessmentResult.FAIL.getResult(), ReviewResult.PASS);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true))
                .isEqualTo(Constants.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenHardshipOverviewResultPassWithFullAssessmentInProgress_whenGetDecisionByFinAssessmentIsInvoked_grantedPassedMeansTestIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.PASS.getResult(), ReviewResult.PASS);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true))
                .isEqualTo(Constants.GRANTED_PASSED_MEANS_TEST);
    }

    @Test
    void givenHardshipOverviewInProgress_whenGetDecisionByFinAssessmentIsInvoked_nullIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        setUpFinAssessment(requestDTO, CurrentStatus.COMPLETE, CurrentStatus.IN_PROGRESS,
                InitAssessmentResult.FULL.getResult(), FullAssessmentResult.PASS.getResult(), ReviewResult.PASS);
        requestDTO.getFinancialAssessment().getHardshipOverview().setAssessmentStatus(CurrentStatus.IN_PROGRESS);
        assertThat(repOrderService.getDecisionByFinAssessment(requestDTO, null, true))
                .isNull();
    }

    @Test
    void givenGrantedDecisionReason_whenDetermineRepTypeByDecisionReasonIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setDecisionReason(DecisionReason.GRANTED);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);

        softly.assertThat(apiCrownCourtSummary.getRepId())
                .isEqualTo(requestDTO.getRepId());

        softly.assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.THROUGH_ORDER);
        softly.assertAll();
    }

    @Test
    void givenDecisionReasonIsFailIoJ_whenDetermineRepTypeByDecisionReasonIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setDecisionReason(DecisionReason.FAILIOJ);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenDecisionReasonIsFailMeans_whenDetermineRepTypeByDecisionReasonIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setDecisionReason(DecisionReason.FAILMEANS);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenDecisionReasonIsFailMEIoJ_whenDetermineRepTypeByDecisionReasonIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setDecisionReason(DecisionReason.FAILMEIOJ);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenDecisionReasonIsAbandoned_whenDetermineRepTypeByDecisionReasonIsInvoked_blankRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setDecisionReason(DecisionReason.ABANDONED);
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        repOrderService.determineRepTypeByDecisionReason(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isBlank();
    }

    @Test
    void givenFailedCFSMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.FAILED_CF_S_FAILED_MEANS_TEST);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.NOT_ELIGIBLE_FOR_REP_ORDER);
    }

    @Test
    void givenFailedIoJAppealFailure_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.FAILED_IO_J_APPEAL_FAILURE);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.NOT_ELIGIBLE_FOR_REP_ORDER);
    }

    @Test
    void givenAppealCCCaseTypeWithGrantedFailedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_FAILED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenCommittalCaseTypeWithGrantedFailedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_repTypeIsBlank() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_FAILED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.COMMITAL);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isBlank();
    }

    @Test
    void givenAppealCCCaseTypeWithGrantedPassported_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSPORTED);
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenAppealCCCaseTypeWithGrantedPassedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenCommittalCaseTypeWithGrantedPassported_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSPORTED);
        requestDTO.setCaseType(CaseType.COMMITAL);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenCommittalCaseTypeWithGrantedPassedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.COMMITAL);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenIndictableCaseTypeWithGrantedPassported_whenDetermineRepTypeByRepOrderDecisionIsInvoked_RepTypeIsBlank() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSPORTED);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isBlank();
    }

    @Test
    void givenIndictableCaseTypeWithGrantedPassedMeansTest_whenDetermineRepTypeByRepOrderDecisionIsInvoked_RepTypeIsBlank() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.GRANTED_PASSED_MEANS_TEST);
        requestDTO.setCaseType(CaseType.INDICTABLE);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isBlank();
    }

    @Test
    void givenRepOrderDecisionIsINEL_whenDetermineRepTypeByRepOrderDecisionIsInvoked_validRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        apiCrownCourtSummary.setRepOrderDecision(Constants.REFUSED_INELIGIBLE);
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        repOrderService.determineRepTypeByRepOrderDecision(requestDTO, apiCrownCourtSummary);
        assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenRepOrderDecisionIsNull_whenDetermineCrownRepTypeIsInvoked_BlankRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.getCrownCourtSummary().setRepOrderDecision(null);
        assertThat(repOrderService.determineCrownRepType(requestDTO).getRepType())
                .isBlank();
    }

    @Test
    void givenSentForTrailWithGranted_whenDetermineCrownRepTypeIsInvoked_ValidRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setMagCourtOutcome(MagCourtOutcome.SENT_FOR_TRIAL);
        requestDTO.setDecisionReason(DecisionReason.GRANTED);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.determineCrownRepType(requestDTO);

        softly.assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.THROUGH_ORDER);

        softly.assertThat(apiCrownCourtSummary.getRepId())
                .isEqualTo(requestDTO.getRepId());
        softly.assertAll();
    }

    @Test
    void givenCommittedForTrailWithGranted_whenDetermineCrownRepTypeIsInvoked_ValidRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setDecisionReason(DecisionReason.GRANTED);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.determineCrownRepType(requestDTO);

        softly.assertThat(apiCrownCourtSummary.getRepType())
                .isEqualTo(Constants.THROUGH_ORDER);

        softly.assertThat(apiCrownCourtSummary.getRepId())
                .isEqualTo(requestDTO.getRepId());
        softly.assertAll();
    }

    @Test
    void givenCCAlreadyCaseType_whenDetermineCrownRepTypeIsInvoked_ValidRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.CC_ALREADY);
        assertThat(repOrderService.determineCrownRepType(requestDTO).getRepType())
                .isEqualTo(Constants.CROWN_COURT_ONLY);
    }

    @Test
    void givenValidWithdrawalDate_whenDetermineCrownRepTypeIsInvoked_ValidRepTypeIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.getCrownCourtSummary().setWithdrawalDate(TestModelDataBuilder.TEST_WITHDRAWAL_DATE);
        assertThat(repOrderService.determineCrownRepType(requestDTO).getRepType())
                .isEqualTo(Constants.DECLINED_REP_ORDER);
    }

    @Test
    void givenRepOrderDecisionIsNull_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsNotChanged() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.getCrownCourtSummary().setRepOrderDecision(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getCrownCourtSummary().getRepOrderDate());
    }

    @Test
    void givenRepOrderDateIsNotNull_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsNotChanged() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getCrownCourtSummary().getRepOrderDate());
    }

    @Test
    void givenIndictableCase_whenDetermineRepOrderDateIsInvoked_DecisionDateIsSetAsRepOrderDate() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.INDICTABLE);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getDecisionDate());
    }

    @Test
    void givenCommittedEitherWayCase_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsNull() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isNull();
    }

    @Test
    void givenEitherWayCase_whenDetermineRepOrderDateIsInvoked_thenRepOrderDateIsCorrect() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
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
        softly.assertAll();
    }

    @Test
    void givenCommittalCase_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsSetToDateReceived() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.COMMITAL);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getDateReceived());
    }

    @Test
    void givenCCAlreadyCase_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsSetToDateReceived() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.CC_ALREADY);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(requestDTO.getDateReceived());
    }

    @Test
    void givenSummaryOnlyCase_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsNull() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isNull();
    }

    @Test
    void givenAppealCCCaseWithExistingIOJAppeal_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsSetToIOJAppealDecisionDate() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        when(maatCourtDataService.getCurrentPassedIOJAppealFromRepId(requestDTO.getRepId(), requestDTO.getLaaTransactionId()))
                .thenReturn(TestModelDataBuilder.getIOJAppealDTO());
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_IOJ_APPEAL_DECISION_DATE);
    }

    @Test
    void givenAppealCCCaseWithNoIOJAppeal_whenDetermineRepOrderDateIsInvoked_RepOrderDateIsSetToDateReceived() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        requestDTO.getCrownCourtSummary().setRepOrderDate(null);
        when(maatCourtDataService.getCurrentPassedIOJAppealFromRepId(requestDTO.getRepId(), requestDTO.getLaaTransactionId()))
                .thenReturn(null);
        assertThat(repOrderService.determineRepOrderDate(requestDTO).getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_DATE_RECEIVED);
    }

    @Test
    void givenAInvalidValidCrownCourt_whenUpdateIsInvoked_thenThrowError() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        when(maatCourtDataService.updateRepOrder(any(), any()))
                .thenThrow(new APIClientException(ERROR_MSG));
        assertThatThrownBy(() -> repOrderService.update(requestDTO))
                .isInstanceOf(APIClientException.class)
                .hasMessageContaining(ERROR_MSG);
    }

    @Test
    void givenAValidCrownCourt_whenUpdateIsInvoked_thenReturnRepOrder() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        repOrderService.update(requestDTO);
        verify(maatCourtDataService).updateRepOrder(any(), any());
    }

    @Test
    void givenAInvalidValidCrownCourt_whenCreateOutcomeIsInvoked_thenThrowError() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        when(maatCourtDataService.createOutcome(any(), any()))
                .thenThrow(new APIClientException(ERROR_MSG));
        assertThatThrownBy(() -> repOrderService.createOutcome(requestDTO))
                .isInstanceOf(APIClientException.class)
                .hasMessageContaining(ERROR_MSG);
    }

    @Test
    void givenANullCrownCourtOutcome_whenCreateOutcomeIsInvoked_thenNotCallCreateOutcome() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.getCrownCourtSummary().setCrownCourtOutcome(null);
        repOrderService.createOutcome(requestDTO);
        verify(maatCourtDataService, times(0)).createOutcome(any(), any());
    }

    @Test
    void givenAEmptyCrownCourtOutcome_whenCreateOutcomeIsInvoked_thenNotCallCreateOutcome() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.getCrownCourtSummary().setCrownCourtOutcome(List.of());
        repOrderService.createOutcome(requestDTO);
        verify(maatCourtDataService, times(0)).createOutcome(any(), any());
    }

    @Test
    void givenAValidCrownCourtOutcome_whenCreateOutcomeIsInvoked_thenOutcomeIsSuccess() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        repOrderService.createOutcome(requestDTO);
        verify(maatCourtDataService, atLeast(1)).createOutcome(any(), any());
    }

    @Test
    void givenAValidCrownCourtInput_whenOutcomeCountIsNotZero_thenReturnEmptyRepOrder() throws Exception {
        when(maatCourtDataService.outcomeCount(any(), any())).thenReturn(1l);
        RepOrderDTO repOrderDTO = repOrderService.updateCCOutcome(TestModelDataBuilder.getCrownCourtDTO());
        verify(maatCourtDataService, atLeastOnce()).outcomeCount(any(), any());
        assertThat(repOrderDTO).isNull();
    }

    @Test
    void givenAOutcomeIsNull_whenUpdateCCOutcomeIsInvoked_thenReturnEmptyRepOrder() throws Exception {
        when(maatCourtDataService.outcomeCount(any(), any())).thenReturn(0l);
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtDTO.getCrownCourtSummary().setCrownCourtOutcome(null);
        RepOrderDTO repOrderDTO = repOrderService.updateCCOutcome(crownCourtDTO);
        verify(maatCourtDataService, atLeastOnce()).outcomeCount(any(), any());
        assertThat(repOrderDTO).isNull();
    }

    @Test
    void givenAOutcomeIsEmpty_whenUpdateCCOutcomeIsInvoked_thenReturnEmptyRepOrder() throws Exception {
        when(maatCourtDataService.outcomeCount(any(), any())).thenReturn(0l);
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtDTO.getCrownCourtSummary().setCrownCourtOutcome(new ArrayList<>());
        RepOrderDTO repOrderDTO = repOrderService.updateCCOutcome(crownCourtDTO);
        verify(maatCourtDataService, atLeastOnce()).outcomeCount(any(), any());
        assertThat(repOrderDTO).isNull();
    }

    @Test
    void givenAEvidenceFeeIsEmpty_whenUpdateCCOutcomeIsInvoked_thenReturnRepOrder() throws Exception {

        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        when(maatCourtDataService.outcomeCount(any(), any())).thenReturn(0l);
        when(crimeEvidenceDataService.getCalEvidenceFee(any())).thenReturn(new ApiCalculateEvidenceFeeResponse());
        when(maatCourtDataService.updateRepOrder(any(), any())).thenReturn(TestModelDataBuilder.getRepOrderDTO());
        RepOrderDTO repOrderDTO = repOrderService.updateCCOutcome(crownCourtDTO);
        verify(maatCourtDataService, atLeastOnce()).outcomeCount(any(), any());
        verify(crimeEvidenceDataService).getCalEvidenceFee(any());
        verify(maatCourtDataService).updateRepOrder(any(), any());
        verify(maatCourtDataService).createOutcome(any(), any());
        assertThat(repOrderDTO).isNotNull();
    }

    @Test
    void givenAValidInput_whenUpdateCCOutcomeIsInvoked_thenReturnRepOrder() throws Exception {

        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        when(maatCourtDataService.outcomeCount(any(), any())).thenReturn(0l);
        when(crimeEvidenceDataService.getCalEvidenceFee(any())).thenReturn(TestModelDataBuilder.getApiCalculateEvidenceFeeResponse());
        when(maatCourtDataService.updateRepOrder(any(), any())).thenReturn(TestModelDataBuilder.getRepOrderDTO());
        RepOrderDTO repOrderDTO = repOrderService.updateCCOutcome(crownCourtDTO);
        verify(maatCourtDataService, atLeastOnce()).outcomeCount(any(), any());
        verify(crimeEvidenceDataService).getCalEvidenceFee(any());
        verify(maatCourtDataService).updateRepOrder(any(), any());
        verify(maatCourtDataService).createOutcome(any(), any());
        assertThat(repOrderDTO).isNotNull();
    }
}
