package uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRepOrderRequestDTO {
    private Integer repId;
    private String caseId;
    private String catyCaseType;
    private String appealTypeCode;
    private String arrestSummonsNo;
    private String userModified;
    private String magsOutcome;
    private String magsOutcomeDate;
    private LocalDate magsOutcomeDateSet;
    private LocalDate committalDate;
    private String repOrderDecisionReasonCode;
    private String crownRepOrderDecision;
    private String crownRepOrderType;
    private LocalDateTime assessmentDateCompleted;
    private LocalDateTime sentenceOrderDate;
}
