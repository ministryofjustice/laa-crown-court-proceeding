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
    private String decisionReasonCode;
    private Integer crownRepId;
    private String crownRepOrderDecision;
    private String crownRepOrderType;
    private LocalDate crownRepOrderDate;
    private LocalDate crownWithdrawalDate;
    private Boolean isImprisoned;
    private LocalDateTime assessmentDateCompleted;
    private LocalDateTime sentenceOrderDate;
    private Integer applicantHistoryId;
    private String evidenceFeeLevel;
    private Integer bankAccountNo;
    private String bankAccountName;
    private String paymentMethod;
    private Integer preferredPaymentDay;
    private String sortCode;

}
