package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsRequest;

public class CrownCourtsActionsRequestDTOBuilder {
    public CrownCourtsActionsRequestDTO buildRequestDTO(final ApiCheckCrownCourtActionsRequest apiCheckCrownCourtActionsRequest) {
        return CrownCourtsActionsRequestDTO.builder()
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
