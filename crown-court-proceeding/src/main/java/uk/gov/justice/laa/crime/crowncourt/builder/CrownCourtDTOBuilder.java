package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateApplicationRequest;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CrownCourtDTOBuilder {

    public static CrownCourtDTO build(final ApiProcessRepOrderRequest request) {
        CrownCourtDTO.CrownCourtDTOBuilder builder = CrownCourtDTO.builder()
                .repId(request.getRepId())
                .caseType(request.getCaseType())
                .magCourtOutcome(request.getMagCourtOutcome())
                .decisionReason(request.getDecisionReason())
                .decisionDate(request.getDecisionDate())
                .committalDate(request.getCommittalDate())
                .dateReceived(request.getDateReceived())
                .crownCourtSummary(request.getCrownCourtSummary())
                .iojAppeal(request.getIojAppeal())
                .financialAssessment(request.getFinancialAssessment())
                .passportAssessment(request.getPassportAssessment());

        if (request instanceof ApiUpdateApplicationRequest updateRequest) {
            return builder.userSession(updateRequest.getUserSession())
                    .crownRepId(updateRequest.getCrownRepId())
                    .applicantHistoryId(updateRequest.getApplicantHistoryId())
                    .isImprisoned(updateRequest.getIsImprisoned())
                    .capitalEvidence(updateRequest.getCapitalEvidence())
                    .incomeEvidenceReceivedDate(updateRequest.getIncomeEvidenceReceivedDate())
                    .capitalEvidenceReceivedDate(updateRequest.getCapitalEvidenceReceivedDate())
                    .emstCode(updateRequest.getEmstCode())
                    .build();
        } else {
            return builder.build();
        }
    }
}
