package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

@Value
@Builder
@AllArgsConstructor
public class ConcludedDTO {

    ProsecutionConcluded prosecutionConcluded;
    String calculatedOutcome;
    String wqJurisdictionType;
    String crownCourtCode;
    List<String> hearingResultCodeList;
    String caseUrn;
    String caseEndDate;
}
