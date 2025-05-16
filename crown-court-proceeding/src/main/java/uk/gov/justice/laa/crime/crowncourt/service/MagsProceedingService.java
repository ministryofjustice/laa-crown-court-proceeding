package uk.gov.justice.laa.crime.crowncourt.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiFinancialAssessment;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiHardshipOverview;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiIOJSummary;
import uk.gov.justice.laa.crime.crowncourt.builder.UpdateRepOrderDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.FinancialAssessmentOutcome;
import uk.gov.justice.laa.crime.enums.*;
import uk.gov.justice.laa.crime.proceeding.MagsDecisionResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagsProceedingService {

    private final MaatCourtDataService maatCourtDataService;

    private final Set<CaseType> magsCourtCaseTypes =
            Set.of(CaseType.INDICTABLE, CaseType.EITHER_WAY, CaseType.SUMMARY_ONLY);

    public MagsDecisionResult determineMagsRepDecision(CrownCourtDTO dto) {
        ApiIOJSummary iojSummary = dto.getIojSummary();
        ReviewResult iojResult =
                ReviewResult.getFrom(
                        iojSummary.getDecisionResult() != null
                                ? iojSummary.getDecisionResult()
                                : iojSummary.getIojResult());
        boolean isMagsCourt = magsCourtCaseTypes.contains(dto.getCaseType());
        if (iojResult != null && isMagsCourt) {
            DecisionReason decisionReason = getDecisionReason(dto, iojResult);
            if (decisionReason != null) {
                MagsDecisionResult decisionResult =
                        MagsDecisionResult.builder()
                                .decisionDate(LocalDate.now())
                                .decisionReason(decisionReason)
                                .build();
                dto.setMagsDecisionResult(decisionResult);
                RepOrderDTO repOrderDTO =
                        maatCourtDataService.updateRepOrder(
                                UpdateRepOrderDTOBuilder.build(dto, decisionResult));
                decisionResult.setTimestamp(repOrderDTO.getDateModified());
                return decisionResult;
            }
        }
        return null;
    }

    private DecisionReason getDecisionReason(CrownCourtDTO dto, ReviewResult iojResult) {
        boolean isIojPassed = (iojResult == ReviewResult.PASS);
        boolean isAssessmentFailed = false;

        if (dto.getPassportAssessment() != null) {
            PassportAssessmentResult passportResult =
                    PassportAssessmentResult.getFrom(dto.getPassportAssessment().getResult());
            boolean isPassported =
                    PassportAssessmentResult.PASS.equals(passportResult)
                            || PassportAssessmentResult.TEMP.equals(passportResult);

            if (isPassported) {
                return getReasonForPass(isIojPassed);
            } else if (passportResult == PassportAssessmentResult.FAIL) {
                isAssessmentFailed = true;
            }
        }

        if (dto.getFinancialAssessment() != null) {
            FinancialAssessmentOutcome outcome =
                    getFinancialAssessmentOutcome(dto.getFinancialAssessment());

            if (outcome == FinancialAssessmentOutcome.PASS) {
                return getReasonForPass(isIojPassed);
            } else if (outcome == FinancialAssessmentOutcome.FAIL) {
                isAssessmentFailed = true;
            }
        }

        // The applicant has failed the passport assessment and/or an initial or full means
        // assessment
        if (isAssessmentFailed) {
            return getReasonForFail(isIojPassed);
        }

        return null;
    }

    private FinancialAssessmentOutcome getFinancialAssessmentOutcome(
            ApiFinancialAssessment financialAssessment) {
        InitAssessmentResult initResult =
                InitAssessmentResult.getFrom(financialAssessment.getInitResult());
        FullAssessmentResult fullResult =
                FullAssessmentResult.getFrom(financialAssessment.getFullResult());
        ReviewResult hardshipResult =
                Optional.ofNullable(financialAssessment.getHardshipOverview())
                        .map(ApiHardshipOverview::getReviewResult)
                        .orElse(null);

        boolean isPass =
                (initResult == InitAssessmentResult.PASS)
                        || (initResult == InitAssessmentResult.FULL
                                && fullResult == FullAssessmentResult.PASS)
                        || (hardshipResult == ReviewResult.PASS);

        boolean isFail =
                (initResult == InitAssessmentResult.FAIL)
                        || ((hardshipResult == null || hardshipResult == ReviewResult.FAIL)
                                && fullResult == FullAssessmentResult.FAIL);

        if (isPass) {
            return FinancialAssessmentOutcome.PASS;
        }

        if (isFail) {
            return FinancialAssessmentOutcome.FAIL;
        }

        return FinancialAssessmentOutcome.NONE;
    }

    private DecisionReason getReasonForPass(boolean isIojPassed) {
        return isIojPassed ? DecisionReason.GRANTED : DecisionReason.FAILIOJ;
    }

    private DecisionReason getReasonForFail(boolean isIojPassed) {
        return isIojPassed ? DecisionReason.FAILMEANS : DecisionReason.FAILMEIOJ;
    }
}
