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
public class CrownCourtApplicationRequestDTO {
    private String laaTransactionId;
    private Integer repId;
    private ApiUserSession userSession;
    private ApiCrownCourtSummary crownCourtSummary;
}
