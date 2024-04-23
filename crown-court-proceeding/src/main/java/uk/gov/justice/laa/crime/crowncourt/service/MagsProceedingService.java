package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.builder.UpdateRepOrderDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.model.MagsDecisionResult;
import uk.gov.justice.laa.crime.crowncourt.model.common.ApiFinancialAssessment;
import uk.gov.justice.laa.crime.crowncourt.model.common.ApiHardshipOverview;
import uk.gov.justice.laa.crime.crowncourt.model.common.ApiIOJSummary;
import uk.gov.justice.laa.crime.enums.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class MagsProceedingService {

    private final MaatCourtDataService maatCourtDataService;

    private final Set<CaseType> magsCourtCaseTypes = Set.of(
            CaseType.INDICTABLE, CaseType.EITHER_WAY, CaseType.SUMMARY_ONLY
    );

    public MagsDecisionResult determineMagsRepDecision(CrownCourtDTO dto) {
        ApiIOJSummary iojSummary = dto.getIojSummary();
        ReviewResult iojResult = ReviewResult.getFrom(
                iojSummary.getDecisionResult() != null
                        ? iojSummary.getDecisionResult() : iojSummary.getIojResult()
        );
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
                        maatCourtDataService.updateRepOrder(UpdateRepOrderDTOBuilder.build(dto, decisionResult));
                decisionResult.setTimestamp(repOrderDTO.getDateModified());
                return decisionResult;
            }
        }
        return null;
    }

    private DecisionReason getDecisionReason(CrownCourtDTO dto, ReviewResult iojResult) {
        boolean isIojPassed = (iojResult == ReviewResult.PASS);
        PassportAssessmentResult passportResult =
                PassportAssessmentResult.getFrom(dto.getPassportAssessment().getResult());
        boolean isPassported = passportResult != null &&
                List.of(PassportAssessmentResult.PASS, PassportAssessmentResult.TEMP)
                        .contains(passportResult);

        ApiFinancialAssessment financialAssessment = dto.getFinancialAssessment();
        InitAssessmentResult initResult = InitAssessmentResult.getFrom(financialAssessment.getInitResult());
        FullAssessmentResult fullResult = FullAssessmentResult.getFrom(financialAssessment.getFullResult());
        ReviewResult hardshipResult = ofNullable(financialAssessment.getHardshipOverview())
                .map(ApiHardshipOverview::getReviewResult)
                .orElse(null);

        if (isPassported || initResult == InitAssessmentResult.PASS || hardshipResult == ReviewResult.PASS
                || (initResult == InitAssessmentResult.FULL && fullResult == FullAssessmentResult.PASS)) {
            return isIojPassed ? DecisionReason.GRANTED : DecisionReason.FAILIOJ;
        } else if (initResult == InitAssessmentResult.FAIL || passportResult == PassportAssessmentResult.FAIL
                || ((hardshipResult == null || hardshipResult == ReviewResult.FAIL) && fullResult == FullAssessmentResult.FAIL)) {
            return isIojPassed ? DecisionReason.FAILMEANS : DecisionReason.FAILIOJ;
        }
        return null;
    }
}
