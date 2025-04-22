package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiHardshipOverview;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiIOJSummary;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiPassportAssessment;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiCalculateEvidenceFeeRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiCalculateEvidenceFeeResponse;
import uk.gov.justice.laa.crime.crowncourt.builder.CrimeEvidenceBuilder;
import uk.gov.justice.laa.crime.crowncourt.builder.OutcomeDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.builder.UpdateRepOrderDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.enums.*;
import uk.gov.justice.laa.crime.util.DateUtil;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepOrderService {

    private final MaatCourtDataService maatCourtDataService;

    private final CrimeEvidenceDataService crimeEvidenceDataService;

    List<String> grantedRepOrderDecisions = List.of(Constants.GRANTED_FAILED_MEANS_TEST,
            Constants.GRANTED_PASSED_MEANS_TEST,
            Constants.GRANTED_PASSPORTED
    );
    List<String> grantedPassRepOrderDecisions = List.of(Constants.GRANTED_PASSED_MEANS_TEST,
            Constants.GRANTED_PASSPORTED
    );
    List<String> failedRepOrderDecisions = List.of(Constants.FAILED_IO_J_APPEAL_FAILURE,
            Constants.FAILED_CF_S_FAILED_MEANS_TEST
    );

    public ApiCrownCourtSummary getRepDecision(CrownCourtDTO requestDTO) {

        ApiCrownCourtSummary apiCrownCourtSummary = requestDTO.getCrownCourtSummary();
        String prevRepOrderDecision = apiCrownCourtSummary.getRepOrderDecision();
        String ccRepOrderDecision;

        ReviewResult reviewResult = getReviewResult(requestDTO.getIojSummary());

        if (requestDTO.getCaseType() == CaseType.APPEAL_CC && reviewResult == ReviewResult.FAIL) {
            ccRepOrderDecision = Constants.FAILED_IO_J_APPEAL_FAILURE;
        } else {
            boolean isValidCaseType =
                    isValidCaseType(requestDTO.getCaseType(), requestDTO.getMagCourtOutcome(), reviewResult);
            ccRepOrderDecision = getDecisionByPassportAssessment(requestDTO.getPassportAssessment(), isValidCaseType);
            if (ccRepOrderDecision == null) {
                ccRepOrderDecision = getDecisionByFinAssessment(requestDTO, reviewResult, isValidCaseType);
            }
        }

        if (prevRepOrderDecision != null && !prevRepOrderDecision.equals(ccRepOrderDecision)) {
            apiCrownCourtSummary.setRepOrderDate(null);
        }
        apiCrownCourtSummary.setRepOrderDecision(ccRepOrderDecision);
        return apiCrownCourtSummary;
    }

    public String getDecisionByFinAssessment(CrownCourtDTO requestDTO, ReviewResult reviewResult, boolean isValidCaseType) {

        if (null != requestDTO.getFinancialAssessment()) {

            FullAssessmentResult fullAssessmentResult = FullAssessmentResult.getFrom(
                    requestDTO.getFinancialAssessment().getFullResult()
            );
            CurrentStatus fullAssessmentStatus = requestDTO.getFinancialAssessment().getFullStatus();
            if (fullAssessmentResult == FullAssessmentResult.INEL
                    && fullAssessmentStatus == CurrentStatus.COMPLETE
                    && (requestDTO.getMagCourtOutcome() == MagCourtOutcome.COMMITTED_FOR_TRIAL
                    || requestDTO.getMagCourtOutcome() == MagCourtOutcome.SENT_FOR_TRIAL)) {
                return Constants.REFUSED_INELIGIBLE;
            }

            InitAssessmentResult initAssessmentResult = InitAssessmentResult.getFrom(
                    requestDTO.getFinancialAssessment().getInitResult()
            );
            CurrentStatus initAssessmentStatus = requestDTO.getFinancialAssessment().getInitStatus();
            ApiHardshipOverview hardshipOverview = requestDTO.getFinancialAssessment().getHardshipOverview();

            if (((initAssessmentResult == InitAssessmentResult.PASS && initAssessmentStatus == CurrentStatus.COMPLETE)
                    || (fullAssessmentResult == FullAssessmentResult.PASS && fullAssessmentStatus == CurrentStatus.COMPLETE)
                    || (hardshipOverview != null && (hardshipOverview.getReviewResult() == ReviewResult.PASS
                    && hardshipOverview.getAssessmentStatus() == CurrentStatus.COMPLETE)))
                    && isValidCaseType) {
                return Constants.GRANTED_PASSED_MEANS_TEST;
            } else if ((initAssessmentResult == InitAssessmentResult.FAIL && initAssessmentStatus == CurrentStatus.COMPLETE)
                    || (fullAssessmentResult == FullAssessmentResult.FAIL && fullAssessmentStatus == CurrentStatus.COMPLETE)) {
                return getDecisionByCaseType(reviewResult, requestDTO.getCaseType(), requestDTO.getMagCourtOutcome());
            }
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
        if (apiPassportAssessment != null) {
            PassportAssessmentResult passportResult = PassportAssessmentResult.getFrom(apiPassportAssessment.getResult());
            if ((passportResult == PassportAssessmentResult.PASS || passportResult == PassportAssessmentResult.TEMP)
                    && apiPassportAssessment.getStatus() == CurrentStatus.COMPLETE
                    && isValidCaseType) {
                return Constants.GRANTED_PASSPORTED;
            }
        }
        return null;
    }

    public boolean isValidCaseType(CaseType caseType, MagCourtOutcome magCourtOutcome, ReviewResult reviewResult) {
        return caseType == CaseType.INDICTABLE || caseType == CaseType.CC_ALREADY || caseType == CaseType.COMMITAL
                || (caseType == CaseType.EITHER_WAY && magCourtOutcome == MagCourtOutcome.COMMITTED_FOR_TRIAL)
                || (caseType == CaseType.APPEAL_CC && reviewResult == ReviewResult.PASS);
    }

    public ReviewResult getReviewResult(ApiIOJSummary iojSummary) {
        if (StringUtils.isNotBlank(iojSummary.getDecisionResult())) {
            return ReviewResult.getFrom(iojSummary.getDecisionResult());
        } else if (StringUtils.isNotBlank(iojSummary.getIojResult())) {
            return ReviewResult.getFrom(iojSummary.getIojResult());
        }
        return null;
    }

    public ApiCrownCourtSummary determineCrownRepType(CrownCourtDTO requestDTO) {
        ApiCrownCourtSummary crownCourtSummary = requestDTO.getCrownCourtSummary();
        if (StringUtils.isNotBlank(crownCourtSummary.getRepOrderDecision())) {
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

    public void determineRepTypeByRepOrderDecision(CrownCourtDTO requestDTO, ApiCrownCourtSummary crownCourtSummary) {
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

    public void determineRepTypeByDecisionReason(CrownCourtDTO requestDTO, ApiCrownCourtSummary crownCourtSummary) {
        DecisionReason decisionReason = requestDTO.getMagsDecisionResult().getDecisionReason();
        if (decisionReason == DecisionReason.GRANTED) {
            crownCourtSummary.setRepType(Constants.THROUGH_ORDER);
            crownCourtSummary.setRepId(requestDTO.getRepId());
        } else if (decisionReason == DecisionReason.FAILIOJ
                || decisionReason == DecisionReason.FAILMEANS
                || decisionReason == DecisionReason.FAILMEIOJ) {
            crownCourtSummary.setRepType(Constants.CROWN_COURT_ONLY);
        }
    }

    public ApiCrownCourtSummary determineRepOrderDate(CrownCourtDTO requestDTO) {
        ApiCrownCourtSummary crownCourtSummary = requestDTO.getCrownCourtSummary();
        String repOrderDecision = crownCourtSummary.getRepOrderDecision();
        if (StringUtils.isNotBlank(repOrderDecision) && crownCourtSummary.getRepOrderDate() == null) {
            switch (requestDTO.getCaseType()) {
                case INDICTABLE -> crownCourtSummary.setRepOrderDate(
                        DateUtil.convertDateToDateTime(requestDTO.getMagsDecisionResult().getDecisionDate())
                );
                case EITHER_WAY -> crownCourtSummary.setRepOrderDate(
                        determineMagsRepOrderDate(requestDTO, repOrderDecision)
                );
                case CC_ALREADY, COMMITAL -> crownCourtSummary.setRepOrderDate(requestDTO.getDateReceived());
                case APPEAL_CC -> {
                    IOJAppealDTO iojAppealDTO = maatCourtDataService
                            .getCurrentPassedIOJAppealFromRepId(requestDTO.getRepId());
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

    private LocalDateTime determineMagsRepOrderDate(CrownCourtDTO requestDTO, String repOrderDecision) {
        DecisionReason decisionReason = requestDTO.getMagsDecisionResult().getDecisionReason();
        List<DecisionReason> failedDecisionReasons =
                List.of(DecisionReason.FAILIOJ, DecisionReason.FAILMEANS, DecisionReason.FAILMEIOJ);

        if (MagCourtOutcome.COMMITTED_FOR_TRIAL.equals(requestDTO.getMagCourtOutcome())) {
            if (DecisionReason.GRANTED.equals(decisionReason)) {
                return DateUtil.convertDateToDateTime(requestDTO.getMagsDecisionResult().getDecisionDate());
            } else if ((decisionReason != null && failedDecisionReasons.contains(decisionReason)) ||
                    Constants.REFUSED_INELIGIBLE.equals(repOrderDecision)) {
                return requestDTO.getCommittalDate();
            }
        }
        return null;
    }

    public RepOrderDTO updateCCOutcome(CrownCourtDTO dto) {
        long repOrderOutcomeCount = maatCourtDataService.outcomeCount(dto.getRepId());
        if (repOrderOutcomeCount == 0 && null != dto.getCrownCourtSummary().getCrownCourtOutcome() &&
                !dto.getCrownCourtSummary().getCrownCourtOutcome().isEmpty()) {

            ApiCalculateEvidenceFeeRequest request = CrimeEvidenceBuilder.build(dto);
            if (null != request.getMagCourtOutcome() && null != request.getEmstCode()
                    && null != request.getCapitalEvidence()) {

                ApiCalculateEvidenceFeeResponse evidenceFeeResponse =
                        crimeEvidenceDataService.calculateEvidenceFee(request);

                if (null != evidenceFeeResponse.getEvidenceFee()) {
                    dto.getCrownCourtSummary().setEvidenceFeeLevel(
                            EvidenceFeeLevel.getFrom(evidenceFeeResponse.getEvidenceFee().getFeeLevel())
                    );
                }
            }
        }
        RepOrderDTO repOrderDTO = update(dto);
        createOutcome(dto);
        return repOrderDTO;
    }


    protected RepOrderDTO update(CrownCourtDTO dto) {
        return maatCourtDataService.updateRepOrder(UpdateRepOrderDTOBuilder.build(dto));
    }

    protected void createOutcome(CrownCourtDTO dto) {
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = OutcomeDTOBuilder.build(dto);
        if (null != repOrderCCOutcomeDTOList) {
            repOrderCCOutcomeDTOList.forEach(maatCourtDataService::createOutcome);
        }
    }
}