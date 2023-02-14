package uk.gov.justice.laa.crime.crowncourt.dto;

import lombok.Builder;
import lombok.Data;
import uk.gov.justice.laa.crime.crowncourt.model.*;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.DecisionReason;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MagCourtOutcome;

import java.time.LocalDateTime;

@Data
@Builder
public class CrownCourtDTO {

    private String laaTransactionId;
    private Integer repId;
    private ApiUserSession userSession;
    private Integer applicantHistoryId;
    private Boolean isImprisoned;
    private ApiPaymentDetails paymentDetails;
    private CaseType caseType;
    private MagCourtOutcome magCourtOutcome;
    private Integer crownRepId;
    private DecisionReason decisionReason;
    private LocalDateTime decisionDate;
    private LocalDateTime committalDate;
    private LocalDateTime dateReceived;
    private ApiCrownCourtSummary crownCourtSummary;
    private ApiIOJAppeal iojAppeal;
    private ApiFinancialAssessment financialAssessment;
    private ApiPassportAssessment passportAssessment;
}
