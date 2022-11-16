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

    public static final String GRANTED_FAILED_MEANS_TEST = "Granted - Failed Means Test";
    public static final String FAILED_CF_S_FAILED_MEANS_TEST = "Failed - CfS Failed Means Test";
    public static final String GRANTED_PASSED_MEANS_TEST = "Granted - Passed Means Test";
    public static final String REFUSED_INELIGIBLE = "Refused - Ineligible";
    public static final String GRANTED_PASSPORTED = "Granted - Passported";
    public static final String FAILED_IO_J_APPEAL_FAILURE = "Failed - IoJ Appeal Failure";

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

}