package uk.gov.justice.laa.crime.crowncourt.dto;

import lombok.Builder;
import lombok.Data;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiFinancialAssessment;
import uk.gov.justice.laa.crime.crowncourt.model.ApiIOJAppeal;
import uk.gov.justice.laa.crime.crowncourt.model.ApiPassportAssessment;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.DecisionReason;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MagCourtOutcome;

import java.time.LocalDateTime;

@Data
@Builder
public class CrownCourtsActionsRequestDTO {
    private String laaTransactionId;
    private Integer repId;
    private CaseType caseType;
    private MagCourtOutcome magCourtOutcome;
    private DecisionReason decisionReason;
    private LocalDateTime decisionDate;
    private LocalDateTime committalDate;
    private LocalDateTime dateReceived;
    private ApiCrownCourtSummary crownCourtSummary;
    private ApiIOJAppeal iojAppeal;
    private ApiFinancialAssessment financialAssessment;
    private ApiPassportAssessment passportAssessment;
}
