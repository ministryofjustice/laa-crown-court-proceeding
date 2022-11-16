package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiIOJAppeal;
import uk.gov.justice.laa.crime.crowncourt.model.ApiPassportAssessment;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepOrderService {

    // Crown Court Rep Decisions
    public static final String GRANTED_FAILED_MEANS_TEST = "Granted - Failed Means Test";
    public static final String FAILED_CF_S_FAILED_MEANS_TEST = "Failed - CfS Failed Means Test";
    public static final String GRANTED_PASSED_MEANS_TEST = "Granted - Passed Means Test";
    public static final String REFUSED_INELIGIBLE = "Refused - Ineligible";
    public static final String GRANTED_PASSPORTED = "Granted - Passported";
    public static final String FAILED_IO_J_APPEAL_FAILURE = "Failed - IoJ Appeal Failure";

    // Crown Court Rep Types
    public static final String CROWN_COURT_ONLY = "Crown Court Only";
    public static final String DECLINED_REP_ORDER = "Declined Rep Order";
    public static final String NOT_ELIGIBLE_FOR_REP_ORDER = "Not eligible for Rep Order";
    public static final String THROUGH_ORDER = "Through Order";

    public ApiCrownCourtSummary getRepDecision(CrownCourtsActionsRequestDTO requestDTO) {

        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        String prevRepOrderDecision = apiCrownCourtSummary.getRepOrderDecision();
        String ccRepOrderDecision;

        ReviewResult reviewResult = getReviewResult(requestDTO.getIojAppeal());

        if (requestDTO.getCaseType() == CaseType.APPEAL_CC && reviewResult == ReviewResult.FAIL) {
            ccRepOrderDecision = FAILED_IO_J_APPEAL_FAILURE;
        } else {
            boolean isValidCaseType = isValidCaseType(requestDTO.getCaseType(), requestDTO.getMagCourtOutcome(), reviewResult);
            ccRepOrderDecision = getDecisionByPassportAssessment(requestDTO.getPassportAssessment(), isValidCaseType);
            if (ccRepOrderDecision == null) {
                ccRepOrderDecision = getDecisionByFinAssessment(requestDTO, reviewResult, isValidCaseType);
            }
        }

        if (!prevRepOrderDecision.equals(ccRepOrderDecision)) {
            apiCrownCourtSummary.setRepOrderDate(null);
        }
        apiCrownCourtSummary.setRepOrderDecision(ccRepOrderDecision);
        return apiCrownCourtSummary;
    }

    public String getDecisionByFinAssessment(CrownCourtsActionsRequestDTO requestDTO, ReviewResult reviewResult, boolean isValidCaseType) {

        FullAssessmentResult fullAssessmentResult = FullAssessmentResult.getFrom(requestDTO.getFinancialAssessment().getFullResult());
        CurrentStatus fullAssessmentStatus = requestDTO.getFinancialAssessment().getFullStatus();
        if (fullAssessmentResult == FullAssessmentResult.INEL
                && fullAssessmentStatus == CurrentStatus.COMPLETE
                && (requestDTO.getMagCourtOutcome() == MagCourtOutcome.COMMITTED_FOR_TRIAL
                || requestDTO.getMagCourtOutcome() == MagCourtOutcome.SENT_FOR_TRIAL)) {
            return REFUSED_INELIGIBLE;
        }

        InitAssessmentResult initAssessmentResult = InitAssessmentResult.getFrom(requestDTO.getFinancialAssessment().getInitResult());
        CurrentStatus initAssessmentStatus = requestDTO.getFinancialAssessment().getInitStatus();
        ReviewResult hardshipOverviewResult = requestDTO.getFinancialAssessment().getHardshipOverview().getReviewResult();
        CurrentStatus hardshipOverviewStatus = requestDTO.getFinancialAssessment().getHardshipOverview().getAssessmentStatus();

        if (((initAssessmentResult == InitAssessmentResult.PASS && initAssessmentStatus == CurrentStatus.COMPLETE)
                || (fullAssessmentResult == FullAssessmentResult.PASS && fullAssessmentStatus == CurrentStatus.COMPLETE)
                || (hardshipOverviewResult == ReviewResult.PASS && hardshipOverviewStatus == CurrentStatus.COMPLETE))
                && isValidCaseType) {
            return GRANTED_PASSED_MEANS_TEST;
        } else if ((initAssessmentResult == InitAssessmentResult.FAIL && initAssessmentStatus == CurrentStatus.COMPLETE)
                || (fullAssessmentResult == FullAssessmentResult.FAIL && fullAssessmentStatus == CurrentStatus.COMPLETE)) {
            return getDecisionByCaseType(reviewResult, requestDTO.getCaseType(), requestDTO.getMagCourtOutcome());
        }
        return null;
    }

    public String getDecisionByCaseType(ReviewResult reviewResult, CaseType caseType, MagCourtOutcome magCourtOutcome) {
        switch (caseType) {
            case COMMITAL:
                return FAILED_CF_S_FAILED_MEANS_TEST;
            case INDICTABLE, CC_ALREADY:
                return GRANTED_FAILED_MEANS_TEST;
            case EITHER_WAY:
                if (magCourtOutcome == MagCourtOutcome.COMMITTED_FOR_TRIAL) {
                    return GRANTED_FAILED_MEANS_TEST;
                }
                break;
            case APPEAL_CC:
                if (reviewResult == ReviewResult.PASS) {
                    return GRANTED_FAILED_MEANS_TEST;
                }
                break;
            default:
                return null;
        }
        return null;
    }

    public String getDecisionByPassportAssessment(ApiPassportAssessment apiPassportAssessment, boolean isValidCaseType) {
        PassportAssessmentResult passportResult = PassportAssessmentResult.getFrom(apiPassportAssessment.getResult());

        if ((passportResult == PassportAssessmentResult.PASS || passportResult == PassportAssessmentResult.TEMP)
                && apiPassportAssessment.getStatus() == CurrentStatus.COMPLETE
                && isValidCaseType) {
            return GRANTED_PASSPORTED;
        }
        return null;
    }

    public boolean isValidCaseType(CaseType caseType, MagCourtOutcome magCourtOutcome, ReviewResult reviewResult) {
        return caseType == CaseType.INDICTABLE || caseType == CaseType.CC_ALREADY || caseType == CaseType.COMMITAL
                || (caseType == CaseType.EITHER_WAY && magCourtOutcome == MagCourtOutcome.COMMITTED_FOR_TRIAL)
                || (caseType == CaseType.APPEAL_CC && reviewResult == ReviewResult.PASS);
    }

    public ReviewResult getReviewResult(ApiIOJAppeal apiIOJAppeal) {
        if (apiIOJAppeal.getDecisionResult() != null) {
            return ReviewResult.getFrom(apiIOJAppeal.getDecisionResult());
        } else if (apiIOJAppeal.getIojResult() != null) {
            return ReviewResult.getFrom(apiIOJAppeal.getIojResult());
        }
        return null;
    }

    public ApiCrownCourtSummary determineCrownRepType(CrownCourtsActionsRequestDTO requestDTO) {
        ApiCrownCourtSummary crownCourtSummary = requestDTO.getCrownCourtSummary();
        /*
            PROCEDURE determine_crown_rep_type (p_app_obj    IN OUT    application_type) IS BEGIN -- determine_crown_rep_type
       if p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_reporder_decision is not null
       THEN
       */
        if (crownCourtSummary.getRepOrderDecision() != null) {
            /*
              IF p_app_obj.mags_outcome_object.outcome IN ('SENT FOR TRIAL','COMMITTED FOR TRIAL')
              THEN
            */
            if (requestDTO.getMagCourtOutcome() == MagCourtOutcome.SENT_FOR_TRIAL
                    || requestDTO.getMagCourtOutcome() == MagCourtOutcome.COMMITTED_FOR_TRIAL) {
                determineRepTypeByDecisionReason(requestDTO, crownCourtSummary);
            }
            determineRepTypeByRepOrderDecision(requestDTO, crownCourtSummary);
            /*
              IF p_app_obj.case_type_object.case_type = 'CC ALREADY'
              THEN
                 p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_REP_TYPE := 'Crown Court Only';
              END IF;
            */
            if (requestDTO.getCaseType() == CaseType.CC_ALREADY) {
                crownCourtSummary.setRepType(CROWN_COURT_ONLY);
            }
            /*
              IF p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_WITHDRAWAL_DATE is not null
              THEN
                 p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_REP_TYPE := 'Declined Rep Order';
              END IF;
            */
            if (crownCourtSummary.getWithdrawalDate() != null) {
                crownCourtSummary.setRepType(DECLINED_REP_ORDER);
            }
        }
        return crownCourtSummary;
    }

    public void determineRepTypeByRepOrderDecision(CrownCourtsActionsRequestDTO requestDTO, ApiCrownCourtSummary crownCourtSummary) {
        /*
          IF p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_reporder_decision like 'Granted%'
           THEN
             IF (p_app_obj.case_type_object.case_type = 'APPEAL CC')
             OR (p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_reporder_decision like 'Granted%Pass%'
                AND p_app_obj.case_type_object.case_type = 'COMMITAL')
             THEN
            p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_REP_TYPE := 'Crown Court Only';
            END IF;
          */
        if ((GRANTED_FAILED_MEANS_TEST.equals(crownCourtSummary.getRepOrderDecision()) && requestDTO.getCaseType() == CaseType.APPEAL_CC) ||
                ((GRANTED_PASSPORTED.equals(crownCourtSummary.getRepOrderDecision()) || GRANTED_PASSED_MEANS_TEST.equals(crownCourtSummary.getRepOrderDecision()))
                        && (requestDTO.getCaseType() == CaseType.APPEAL_CC || requestDTO.getCaseType() == CaseType.COMMITAL))) {
            crownCourtSummary.setRepType(CROWN_COURT_ONLY);
        }
        /*
         ELSIF p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_reporder_decision like 'Failed%' THEN
         p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_REP_TYPE := 'Not eligible for Rep Order';
         END IF;
        */
        else if (FAILED_CF_S_FAILED_MEANS_TEST.equals(crownCourtSummary.getRepOrderDecision()) ||
                FAILED_IO_J_APPEAL_FAILURE.equals(crownCourtSummary.getRepOrderDecision())) {
            crownCourtSummary.setRepType(NOT_ELIGIBLE_FOR_REP_ORDER);
        }
    }

    public void determineRepTypeByDecisionReason(CrownCourtsActionsRequestDTO requestDTO, ApiCrownCourtSummary crownCourtSummary) {
    /*
     IF p_app_obj.decision_reason_object.code = 'GRANTED'
     THEN
     */
        if (requestDTO.getDecisionReason() == DecisionReason.GRANTED) {
            /*
            p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_REP_TYPE := 'Through Order';
            p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_REP_ID   := p_app_obj.rep_id;
            */
            crownCourtSummary.setRepType(THROUGH_ORDER);
            crownCourtSummary.setRepId(requestDTO.getRepId());
        /*
         ELSIF p_app_obj.decision_reason_object.code like 'FAIL%'
         THEN
            p_app_obj.crown_court_overview_object.crown_court_summary_object.CC_REP_TYPE := 'Crown Court Only';
         END IF;
      END IF;
         */
        } else if (requestDTO.getDecisionReason() == DecisionReason.FAILIOJ
                || requestDTO.getDecisionReason() == DecisionReason.FAILMEANS
                || requestDTO.getDecisionReason() == DecisionReason.FAILMEIOJ) {
            crownCourtSummary.setRepType(CROWN_COURT_ONLY);
        }
    }
}