package uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.crime.enums.EvidenceFeeLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepOrderDTO {
    private Integer id;
    private String caseId;
    private String catyCaseType;
    private String appealTypeCode;
    private String arrestSummonsNo;
    private String userModified;
    private LocalDateTime dateModified;
    private String magsOutcome;
    private String magsOutcomeDate;
    private LocalDate magsOutcomeDateSet;
    private LocalDate committalDate;
    @JsonIgnore
    @Builder.Default
    private List<PassportAssessmentDTO> passportAssessments = new ArrayList<>();
    @JsonIgnore
    @Builder.Default
    private List<FinancialAssessmentDTO> financialAssessments = new ArrayList<>();
    @Builder.Default
    private List<RepOrderCCOutcomeDTO> repOrderCCOutcome = new ArrayList<>();
    private String decisionReasonCode;
    private String crownRepOrderDecision;
    private String crownRepOrderType;
    private LocalDate crownRepOrderDate;
    private LocalDate assessmentDateCompleted;
    private LocalDate sentenceOrderDate;
    private EvidenceFeeLevel evidenceFeeLevel;
}
