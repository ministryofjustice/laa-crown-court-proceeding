package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiDetermineMagsRepDecisionRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateCrownCourtRequest;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.proceeding.MagsDecisionResult;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CrownCourtDTOBuilder {

    public static CrownCourtDTO build(final ApiProcessRepOrderRequest request) {
        CrownCourtDTO.CrownCourtDTOBuilder builder = mapToCrownCourtDTO(request);

        if (request instanceof ApiUpdateApplicationRequest updateRequest) {
            mapUpdateApplicationRequestToCrownCourtDTO(updateRequest, builder);
        }
        if (request instanceof ApiUpdateCrownCourtRequest crownCourtRequest) {
            mapUpdateCrownCourtRequestToCrownCourtDTO(crownCourtRequest, builder);
        }
        return builder.build();
    }

    private static CrownCourtDTO.CrownCourtDTOBuilder mapToCrownCourtDTO(final ApiProcessRepOrderRequest request) {
        return CrownCourtDTO.builder()
                .repId(request.getRepId())
                .caseType(request.getCaseType())
                .magCourtOutcome(request.getMagCourtOutcome())
                .magsDecisionResult(MagsDecisionResult.builder()
                        .decisionReason(request.getDecisionReason())
                        .decisionDate(Optional.ofNullable(request.getDecisionDate())
                                .map(LocalDateTime::toLocalDate)
                                .orElse(null))
                        .build())
                .committalDate(request.getCommittalDate())
                .dateReceived(request.getDateReceived())
                .crownCourtSummary(request.getCrownCourtSummary())
                .iojSummary(request.getIojAppeal())
                .financialAssessment(request.getFinancialAssessment())
                .passportAssessment(request.getPassportAssessment());
    }

    private static void mapUpdateApplicationRequestToCrownCourtDTO(
            ApiUpdateApplicationRequest updateRequest, CrownCourtDTO.CrownCourtDTOBuilder builder) {

        builder.userSession(updateRequest.getUserSession())
                .crownRepId(updateRequest.getCrownRepId())
                .applicantHistoryId(updateRequest.getApplicantHistoryId())
                .isImprisoned(updateRequest.getIsImprisoned());
    }

    public static CrownCourtDTO build(final ApiDetermineMagsRepDecisionRequest request) {
        return CrownCourtDTO.builder()
                .repId(request.getRepId())
                .caseType(request.getCaseType())
                .passportAssessment(request.getPassportAssessment())
                .iojSummary(request.getIojAppeal())
                .financialAssessment(request.getFinancialAssessment())
                .userSession(request.getUserSession())
                .build();
    }

    private static void mapUpdateCrownCourtRequestToCrownCourtDTO(
            final ApiUpdateCrownCourtRequest request, CrownCourtDTO.CrownCourtDTOBuilder builder) {
        builder.capitalEvidence(request.getCapitalEvidence())
                .incomeEvidenceReceivedDate(request.getIncomeEvidenceReceivedDate())
                .capitalEvidenceReceivedDate(request.getCapitalEvidenceReceivedDate())
                .emstCode(request.getEmstCode())
                .build();
    }
}
