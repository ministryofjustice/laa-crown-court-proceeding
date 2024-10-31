package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.time.LocalDate;
import java.util.*;

@Component
public class CaseConclusionDTOBuilder {

    public ConcludedDTO build(ProsecutionConcluded prosecutionConcluded, WQHearingDTO wqHearingDTO, String calculatedOutcome, String crownCourtCode) {
        return ConcludedDTO.
                builder()
                .prosecutionConcluded(prosecutionConcluded)
                .calculatedOutcome(calculatedOutcome)
                .crownCourtCode(crownCourtCode)
                .wqJurisdictionType(wqHearingDTO.getWqJurisdictionType())
                .caseEndDate(getMostRecentCaseEndDate(prosecutionConcluded.getOffenceSummary()))
                .caseUrn(wqHearingDTO.getCaseUrn())
                .hearingResultCodeList(buildResultCodeList(wqHearingDTO))
                .build();
    }

    protected String getMostRecentCaseEndDate(List<OffenceSummary> offenceSummaryList) {

        if (offenceSummaryList == null || offenceSummaryList.isEmpty()) {
            return null;
        }
        Optional<LocalDate> caseEndDate = offenceSummaryList.stream()
                .filter(offenceSummary -> StringUtils.isNotBlank(offenceSummary.getProceedingsConcludedChangedDate()))
                .map(offenceSummary -> LocalDate.parse(offenceSummary.getProceedingsConcludedChangedDate()))
                .distinct().toList()
                .stream()
                .sorted(Comparator.reverseOrder())
                .findFirst();

        return caseEndDate.isPresent() ? caseEndDate.get().toString() : null;
    }


    protected List<String> buildResultCodeList(WQHearingDTO wqHearingDTO) {
        String results = wqHearingDTO.getResultCodes() != null ? wqHearingDTO.getResultCodes() : "";
        List<String> list = new ArrayList<>();
        Set<String> uniqueValues = new HashSet<>();
        for (String s : results.split(",")) {
            if (uniqueValues.add(s)) {
                list.add(s);
            }
        }
        return list;
    }
}
