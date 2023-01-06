package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.ProcessCrownRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessCrownRepOrderRequest;

public class ProcessCrownRepOrderRequestDTOBuilder {
    public ProcessCrownRepOrderRequestDTO buildRequestDTO(final ApiProcessCrownRepOrderRequest apiProcessCrownRepOrderRequest) {
        return ProcessCrownRepOrderRequestDTO.builder()
                .laaTransactionId(apiProcessCrownRepOrderRequest.getLaaTransactionId())
                .repId(apiProcessCrownRepOrderRequest.getRepId())
                .caseType(apiProcessCrownRepOrderRequest.getCaseType())
                .magCourtOutcome(apiProcessCrownRepOrderRequest.getMagCourtOutcome())
                .decisionReason(apiProcessCrownRepOrderRequest.getDecisionReason())
                .decisionDate(apiProcessCrownRepOrderRequest.getDecisionDate())
                .committalDate(apiProcessCrownRepOrderRequest.getCommittalDate())
                .dateReceived(apiProcessCrownRepOrderRequest.getDateReceived())
                .crownCourtSummary(apiProcessCrownRepOrderRequest.getCrownCourtSummary())
                .iojAppeal(apiProcessCrownRepOrderRequest.getIojAppeal())
                .financialAssessment(apiProcessCrownRepOrderRequest.getFinancialAssessment())
                .passportAssessment(apiProcessCrownRepOrderRequest.getPassportAssessment())
                .build();
    }
}
