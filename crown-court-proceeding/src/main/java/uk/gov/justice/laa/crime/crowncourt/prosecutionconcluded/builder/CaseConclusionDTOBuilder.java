package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.builder;

import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.entity.WQHearingEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model.ProsecutionConcluded;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CaseConclusionDTOBuilder {

    public ConcludedDTO build(ProsecutionConcluded prosecutionConcluded, WQHearingEntity wqHearingEntity, String calculatedOutcome) {
        return ConcludedDTO.
                builder()
                .prosecutionConcluded(prosecutionConcluded)
                .calculatedOutcome(calculatedOutcome)
                .ouCourtLocation(wqHearingEntity.getOuCourtLocation())
                .wqJurisdictionType(wqHearingEntity.getWqJurisdictionType())
                .caseEndDate(getMostRecentCaseEndDate(prosecutionConcluded.getOffenceSummary()))
                .caseUrn(wqHearingEntity.getCaseUrn())
                .hearingResultCodeList(buildResultCodeList(wqHearingEntity))
                .build();
    }

    private String getMostRecentCaseEndDate(List<OffenceSummary> offenceSummaryList) {

        if (offenceSummaryList == null || offenceSummaryList.isEmpty()) {
            return null;
        }
        return offenceSummaryList.stream()
                .map(offenceSummary -> LocalDate.parse(offenceSummary.getProceedingsConcludedChangedDate()))
                .distinct()
                .collect(Collectors.toList())
                .stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList()).get(0).toString();
    }

    private List<String> buildResultCodeList(WQHearingEntity wqHearingEntity) {
        String results = wqHearingEntity.getResultCodes() != null ? wqHearingEntity.getResultCodes() : "";
        return Arrays.stream(results.split(","))
                .distinct()
                .collect(Collectors.toList());
    }
}
