package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.PleaTrialOutcome;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.VerdictTrialOutcome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalculateOutcomeHelper {

    private final ProsecutionConcludedDataService prosecutionConcludedDataService;

    public String calculate(List<OffenceSummary> offenceSummaryList) {
        List<String> outcomes = buildOffenceOutComes(offenceSummaryList);

        log.info("Offence count: " + outcomes.size());
        return outcomes.size() == 1 ? outcomes.get(0) : CrownCourtTrialOutcome.PART_CONVICTED.getValue();
    }

    private List<String> buildOffenceOutComes(List<OffenceSummary> offenceSummaryList) {
        List<String> offenceOutcomeList = new ArrayList<>();
        offenceSummaryList
                .forEach(offence -> {
                    if (isVerdictAvailable(offence)) {
                        offenceOutcomeList.add(VerdictTrialOutcome.getTrialOutcome(offence.getVerdict().getVerdictType().getCategoryType()));
                        if (isVerdictPleaMismatch(offence)) {
                            log.error("The recent Plea outcome is different from the Verdict outcome - " +
                                            "Offence Id {} Plea Date {} Verdict Date {} Plea Outcome {} Verdict Outcome {}",
                                    offence.getOffenceId(), offence.getPlea().getPleaDate(), offence.getVerdict().getVerdictDate(),
                                    PleaTrialOutcome.getTrialOutcome(offence.getPlea().getValue()),
                                    VerdictTrialOutcome.getTrialOutcome(offence.getVerdict().getVerdictType().getCategoryType())
                            );
                        }
                    } else if (offence.getPlea() != null && offence.getPlea().getValue() != null) {
                        offenceOutcomeList.add(PleaTrialOutcome.getTrialOutcome(offence.getPlea().getValue()));
                    } else {
                        offenceOutcomeList.add(CrownCourtTrialOutcome.AQUITTED.getValue());
                    }
                });

        List<String> list = new ArrayList<>();
        Set<String> uniqueValues = new HashSet<>();
        for (String s : offenceOutcomeList) {
            if (uniqueValues.add(s)) {
                list.add(s);
            }
        }
        return list;

    }

    private boolean isVerdictPleaMismatch(OffenceSummary offence) {
        if ((offence.getPlea() != null && offence.getPlea().getPleaDate() != null)
                && offence.getVerdict().getVerdictDate().compareTo(offence.getPlea().getPleaDate()) < 0) {
            String verdictOutcome = VerdictTrialOutcome.getTrialOutcome(offence.getVerdict().getVerdictType().getCategoryType());
            String pleaOutcome = PleaTrialOutcome.getTrialOutcome(offence.getPlea().getValue());
            return !verdictOutcome.equalsIgnoreCase(pleaOutcome);
        }
        return false;
    }

    private boolean isVerdictAvailable(OffenceSummary offence) {
        return offence.getVerdict() != null
                && offence.getVerdict().getVerdictType() != null
                && offence.getVerdict().getVerdictType().getCategoryType() != null;
    }
}
