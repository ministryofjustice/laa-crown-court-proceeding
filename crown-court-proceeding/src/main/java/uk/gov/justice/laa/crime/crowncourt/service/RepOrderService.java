package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtApplicationRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiIOJAppeal;
import uk.gov.justice.laa.crime.crowncourt.model.ApiPassportAssessment;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.*;

import java.time.LocalDateTime;
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

    public ApiCrownCourtSummary getRepDecision(CrownCourtActionsRequestDTO requestDTO) {

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

    public String getDecisionByFinAssessment(CrownCourtActionsRequestDTO requestDTO, ReviewResult reviewResult, boolean isValidCaseType) {

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
            case COMMITAL -> {
                return Constants.FAILED_CF_S_FAILED_MEANS_TEST;
            }
            case INDICTABLE, CC_ALREADY -> {
                return Constants.GRANTED_FAILED_MEANS_TEST;
            }
            case EITHER_WAY -> {
                if (magCourtOutcome == MagCourtOutcome.COMMITTED_FOR_TRIAL) {
                    return Constants.GRANTED_FAILED_MEANS_TEST;
                }
            }
            case APPEAL_CC -> {
                if (reviewResult == ReviewResult.PASS) {
                    return Constants.GRANTED_FAILED_MEANS_TEST;
                }
            }
            default -> {
                return null;
            }
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

    public ApiCrownCourtSummary determineCrownRepType(CrownCourtActionsRequestDTO requestDTO) {
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

    public void determineRepTypeByRepOrderDecision(CrownCourtActionsRequestDTO requestDTO, ApiCrownCourtSummary crownCourtSummary) {
        CaseType caseType = requestDTO.getCaseType();
        String repOrderDecision = crownCourtSummary.getRepOrderDecision();
        if ((grantedRepOrderDecisions.contains(repOrderDecision) && caseType == CaseType.APPEAL_CC) ||
                (grantedPassRepOrderDecisions.contains(repOrderDecision) && caseType == CaseType.COMMITAL) ||
                Constants.REFUSED_INELIGIBLE.equals(repOrderDecision)) {
            crownCourtSummary.setRepType(Constants.CROWN_COURT_ONLY);
        } else if (failedRepOrderDecisions.contains(repOrderDecision)) {
            crownCourtSummary.setRepType(Constants.NOT_ELIGIBLE_FOR_REP_ORDER);
        }
    }

    public void determineRepTypeByDecisionReason(CrownCourtActionsRequestDTO requestDTO, ApiCrownCourtSummary crownCourtSummary) {
        if (requestDTO.getDecisionReason() == DecisionReason.GRANTED) {
            crownCourtSummary.setRepType(Constants.THROUGH_ORDER);
            crownCourtSummary.setRepId(requestDTO.getRepId());
        } else if (requestDTO.getDecisionReason() == DecisionReason.FAILIOJ
                || requestDTO.getDecisionReason() == DecisionReason.FAILMEANS
                || requestDTO.getDecisionReason() == DecisionReason.FAILMEIOJ) {
            crownCourtSummary.setRepType(Constants.CROWN_COURT_ONLY);
        }
    }

    public ApiCrownCourtSummary determineRepOrderDate(CrownCourtActionsRequestDTO requestDTO) {
        ApiCrownCourtSummary crownCourtSummary = requestDTO.getCrownCourtSummary();
        String repOrderDecision = crownCourtSummary.getRepOrderDecision();
        if (repOrderDecision != null && crownCourtSummary.getRepOrderDate() == null) {
            switch (requestDTO.getCaseType()) {
                case INDICTABLE -> crownCourtSummary.setRepOrderDate(requestDTO.getDecisionDate());
                case EITHER_WAY -> crownCourtSummary.setRepOrderDate(
                        determineMagsRepOrderDate(requestDTO, repOrderDecision)
                );
                case CC_ALREADY, COMMITAL -> crownCourtSummary.setRepOrderDate(requestDTO.getDateReceived());
                case APPEAL_CC -> {
                    IOJAppealDTO iojAppealDTO = maatCourtDataService
                            .getCurrentPassedIOJAppealFromRepId(requestDTO.getRepId(), requestDTO.getLaaTransactionId());
                    if (iojAppealDTO != null) {
                        crownCourtSummary.setRepOrderDate(iojAppealDTO.getDecisionDate());
                    } else {
                        crownCourtSummary.setRepOrderDate(requestDTO.getDateReceived());
                    }
                }
                default -> crownCourtSummary.setRepOrderDate(null);
            }
        }
        return crownCourtSummary;
    }

    private LocalDateTime determineMagsRepOrderDate(CrownCourtActionsRequestDTO requestDTO, String repOrderDecision) {
        DecisionReason decisionReason = requestDTO.getDecisionReason();
        List<DecisionReason> failedDecisionReasons =
                List.of(DecisionReason.FAILIOJ, DecisionReason.FAILMEANS, DecisionReason.FAILMEIOJ);

        if (MagCourtOutcome.COMMITTED_FOR_TRIAL.equals(requestDTO.getMagCourtOutcome())) {
            if (DecisionReason.GRANTED.equals(decisionReason)) {
                return requestDTO.getDecisionDate();
            } else if (failedDecisionReasons.contains(decisionReason) ||
                    Constants.REFUSED_INELIGIBLE.equals(repOrderDecision)) {
                return requestDTO.getCommittalDate();
            }
        }
        return null;
    }

    public void updateCCSentenceOrderDate(CrownCourtApplicationRequestDTO crownCourtApplicationRequestDTO) {
        UpdateRepOrderRequestDTO build = UpdateRepOrderRequestDTO.builder()
                .repId(crownCourtApplicationRequestDTO.getRepId())
                .sentenceOrderDate(crownCourtApplicationRequestDTO.getCrownCourtSummary().getSentenceOrderDate())
                .userModified(crownCourtApplicationRequestDTO.getUserSession().getUserName())
                .build();
        maatCourtDataService.updateRepOrder(build, crownCourtApplicationRequestDTO.getLaaTransactionId());
    }
}