package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model.ProsecutionConcluded;

import java.util.List;

@Value
@Builder
@AllArgsConstructor
public class ConcludedDTO {

    ProsecutionConcluded prosecutionConcluded;
    String calculatedOutcome;
    String wqJurisdictionType;
    String ouCourtLocation;
    List<String> hearingResultCodeList;
    String caseUrn;
    String caseEndDate;
}
