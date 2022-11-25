package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiIOJAppeal;
import uk.gov.justice.laa.crime.crowncourt.model.ApiPassportAssessment;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.*;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepOrderService {

    private final MaatCourtDataService maatCourtDataService;
    List<String> grantedRepOrderDecisions = List.of(Constants.GRANTED_FAILED_MEANS_TEST,
            Constants.GRANTED_PASSED_MEANS_TEST,
            Constants.GRANTED_PASSPORTED);
    List<String> grantedPassRepOrderDecisions = List.of(Constants.GRANTED_PASSED_MEANS_TEST,
            Constants.GRANTED_PASSPORTED);
    List<String> failedRepOrderDecisions = List.of(Constants.FAILED_IO_J_APPEAL_FAILURE,
            Constants.FAILED_CF_S_FAILED_MEANS_TEST);

    public ApiCrownCourtSummary getRepDecision(CrownCourtsActionsRequestDTO requestDTO) {

        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        String prevRepOrderDecision = apiCrownCourtSummary.getRepOrderDecision();
        String ccRepOrderDecision;

        ReviewResult reviewResult = getReviewResult(requestDTO.getIojAppeal());

        if (requestDTO.getCaseType() == CaseType.APPEAL_CC && reviewResult == ReviewResult.FAIL) {
            ccRepOrderDecision = Constants.FAILED_IO_J_APPEAL_FAILURE;
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
            return Constants.REFUSED_INELIGIBLE;
        }

        InitAssessmentResult initAssessmentResult = InitAssessmentResult.getFrom(requestDTO.getFinancialAssessment().getInitResult());
        CurrentStatus initAssessmentStatus = requestDTO.getFinancialAssessment().getInitStatus();
        ReviewResult hardshipOverviewResult = requestDTO.getFinancialAssessment().getHardshipOverview().getReviewResult();
        CurrentStatus hardshipOverviewStatus = requestDTO.getFinancialAssessment().getHardshipOverview().getAssessmentStatus();

        if (((initAssessmentResult == InitAssessmentResult.PASS && initAssessmentStatus == CurrentStatus.COMPLETE)
                || (fullAssessmentResult == FullAssessmentResult.PASS && fullAssessmentStatus == CurrentStatus.COMPLETE)
                || (hardshipOverviewResult == ReviewResult.PASS && hardshipOverviewStatus == CurrentStatus.COMPLETE))
                && isValidCaseType) {
            return Constants.GRANTED_PASSED_MEANS_TEST;
        } else if ((initAssessmentResult == InitAssessmentResult.FAIL && initAssessmentStatus == CurrentStatus.COMPLETE)
                || (fullAssessmentResult == FullAssessmentResult.FAIL && fullAssessmentStatus == CurrentStatus.COMPLETE)) {
            return getDecisionByCaseType(reviewResult, requestDTO.getCaseType(), requestDTO.getMagCourtOutcome());
        }
        return null;
    }

    public String getDecisionByCaseType(ReviewResult reviewResult, CaseType caseType, MagCourtOutcome magCourtOutcome) {
        switch (caseType) {
            case COMMITAL:
                return Constants.FAILED_CF_S_FAILED_MEANS_TEST;
            case INDICTABLE, CC_ALREADY:
                return Constants.GRANTED_FAILED_MEANS_TEST;
            case EITHER_WAY:
                if (magCourtOutcome == MagCourtOutcome.COMMITTED_FOR_TRIAL) {
                    return Constants.GRANTED_FAILED_MEANS_TEST;
                }
                break;
            case APPEAL_CC:
                if (reviewResult == ReviewResult.PASS) {
                    return Constants.GRANTED_FAILED_MEANS_TEST;
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
            return Constants.GRANTED_PASSPORTED;
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
        if (crownCourtSummary.getRepOrderDecision() != null) {
            if (requestDTO.getMagCourtOutcome() == MagCourtOutcome.SENT_FOR_TRIAL
                    || requestDTO.getMagCourtOutcome() == MagCourtOutcome.COMMITTED_FOR_TRIAL) {
                determineRepTypeByDecisionReason(requestDTO, crownCourtSummary);
            }
            determineRepTypeByRepOrderDecision(requestDTO, crownCourtSummary);

            if (requestDTO.getCaseType() == CaseType.CC_ALREADY) {
                crownCourtSummary.setRepType(Constants.CROWN_COURT_ONLY);
            }

            if (crownCourtSummary.getWithdrawalDate() != null) {
                crownCourtSummary.setRepType(Constants.DECLINED_REP_ORDER);
            }
        }
        return crownCourtSummary;
    }

    public void determineRepTypeByRepOrderDecision(CrownCourtsActionsRequestDTO requestDTO, ApiCrownCourtSummary crownCourtSummary) {
        if ((grantedRepOrderDecisions.contains(crownCourtSummary.getRepOrderDecision()) && requestDTO.getCaseType() == CaseType.APPEAL_CC) ||
                (grantedPassRepOrderDecisions.contains(crownCourtSummary.getRepOrderDecision()) && requestDTO.getCaseType() == CaseType.COMMITAL)) {
            crownCourtSummary.setRepType(Constants.CROWN_COURT_ONLY);
        } else if (failedRepOrderDecisions.contains(crownCourtSummary.getRepOrderDecision())) {
            crownCourtSummary.setRepType(Constants.NOT_ELIGIBLE_FOR_REP_ORDER);
        }
    }

    public void determineRepTypeByDecisionReason(CrownCourtsActionsRequestDTO requestDTO, ApiCrownCourtSummary crownCourtSummary) {
        if (requestDTO.getDecisionReason() == DecisionReason.GRANTED) {
            crownCourtSummary.setRepType(Constants.THROUGH_ORDER);
            crownCourtSummary.setRepId(requestDTO.getRepId());
        } else if (requestDTO.getDecisionReason() == DecisionReason.FAILIOJ
                || requestDTO.getDecisionReason() == DecisionReason.FAILMEANS
                || requestDTO.getDecisionReason() == DecisionReason.FAILMEIOJ) {
            crownCourtSummary.setRepType(Constants.CROWN_COURT_ONLY);
        }
    }

    public ApiCrownCourtSummary determineRepOrderDate(CrownCourtsActionsRequestDTO requestDTO) {
        ApiCrownCourtSummary crownCourtSummary = requestDTO.getCrownCourtSummary();
        if (crownCourtSummary.getRepOrderDecision() != null && crownCourtSummary.getRepOrderDate() == null) {
            switch (requestDTO.getCaseType()) {
                case INDICTABLE:
                    crownCourtSummary.setRepOrderDate(requestDTO.getDecisionDate());
                    break;
                case EITHER_WAY:
                    if (requestDTO.getMagCourtOutcome() == MagCourtOutcome.COMMITTED_FOR_TRIAL) {
                        switch (requestDTO.getDecisionReason()) {
                            case GRANTED -> crownCourtSummary.setRepOrderDate(requestDTO.getDecisionDate());
                            case FAILIOJ, FAILMEIOJ, FAILMEANS -> crownCourtSummary.setRepOrderDate(requestDTO.getCommittalDate());
                            default -> crownCourtSummary.setRepOrderDate(null);
                        }
                    }
                    break;
                case CC_ALREADY, COMMITAL:
                    crownCourtSummary.setRepOrderDate(requestDTO.getDateReceived());
                    break;
                case APPEAL_CC:
                    try {
                        IOJAppealDTO iojAppealDTO = maatCourtDataService
                                .getCurrentPassedIOJAppealFromRepId(requestDTO.getRepId(), requestDTO.getLaaTransactionId());
                        crownCourtSummary.setRepOrderDate(iojAppealDTO.getDecisionDate());
                    } catch (Exception ex) {
                        crownCourtSummary.setRepOrderDate(requestDTO.getDateReceived());
                    }
                    break;
                default:
            }
        }
        return crownCourtSummary;
    }
}