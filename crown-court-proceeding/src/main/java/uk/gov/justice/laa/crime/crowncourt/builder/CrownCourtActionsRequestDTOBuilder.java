package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsRequest;

public class CrownCourtActionsRequestDTOBuilder {
    public CrownCourtActionsRequestDTO buildRequestDTO(final ApiCheckCrownCourtActionsRequest apiCheckCrownCourtActionsRequest) {
        return CrownCourtActionsRequestDTO.builder()
                .laaTransactionId(apiCheckCrownCourtActionsRequest.getLaaTransactionId())
                .repId(apiCheckCrownCourtActionsRequest.getRepId())
                .caseType(apiCheckCrownCourtActionsRequest.getCaseType())
                .magCourtOutcome(apiCheckCrownCourtActionsRequest.getMagCourtOutcome())
                .decisionReason(apiCheckCrownCourtActionsRequest.getDecisionReason())
                .decisionDate(apiCheckCrownCourtActionsRequest.getDecisionDate())
                .committalDate(apiCheckCrownCourtActionsRequest.getCommittalDate())
                .dateReceived(apiCheckCrownCourtActionsRequest.getDateReceived())
                .crownCourtSummary(apiCheckCrownCourtActionsRequest.getCrownCourtSummary())
                .iojAppeal(apiCheckCrownCourtActionsRequest.getIojAppeal())
                .financialAssessment(apiCheckCrownCourtActionsRequest.getFinancialAssessment())
                .passportAssessment(apiCheckCrownCourtActionsRequest.getPassportAssessment())
                .build();
    }
}
