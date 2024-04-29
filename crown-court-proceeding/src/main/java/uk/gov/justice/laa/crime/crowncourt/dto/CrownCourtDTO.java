package uk.gov.justice.laa.crime.crowncourt.dto;

import lombok.Builder;
import lombok.Data;
import uk.gov.justice.laa.crime.crowncourt.model.MagsDecisionResult;
import uk.gov.justice.laa.crime.crowncourt.model.common.*;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.EvidenceFeeLevel;
import uk.gov.justice.laa.crime.enums.MagCourtOutcome;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CrownCourtDTO {

    private Integer repId;
    private ApiUserSession userSession;
    private Integer applicantHistoryId;
    private Boolean isImprisoned;
    private CaseType caseType;
    private MagCourtOutcome magCourtOutcome;
    private Integer crownRepId;
    private MagsDecisionResult magsDecisionResult;
    private LocalDateTime committalDate;
    private LocalDateTime dateReceived;
    private EvidenceFeeLevel evidenceFeeLevel;
    private ApiCrownCourtSummary crownCourtSummary;
    private LocalDateTime incomeEvidenceReceivedDate;
    private LocalDateTime capitalEvidenceReceivedDate;
    private String emstCode;
    private List<ApiCapitalEvidence> capitalEvidence;
    private ApiIOJSummary iojSummary;
    private ApiFinancialAssessment financialAssessment;
    private ApiPassportAssessment passportAssessment;
}
