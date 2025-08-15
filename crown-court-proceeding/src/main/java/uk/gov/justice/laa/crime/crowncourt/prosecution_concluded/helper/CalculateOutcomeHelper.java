package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.enums.CallerType;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.PleaTrialOutcome;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.VerdictTrialOutcome;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Result;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.enums.CallerType.QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalculateOutcomeHelper {

    private final ProsecutionConcludedDataService prosecutionConcludedDataService;

    public String calculate(List<OffenceSummary> offenceSummaryList, ProsecutionConcluded prosecutionConcluded, CallerType callerType) {
        List<String> outcomes = buildOffenceOutComes(offenceSummaryList, prosecutionConcluded, callerType);

        log.info("Offence count: " + outcomes.size());
        return outcomes.size() == 1 ? outcomes.get(0) : CrownCourtTrialOutcome.PART_CONVICTED.getValue();
    }

    private List<String> buildOffenceOutComes(List<OffenceSummary> offenceSummaryList, ProsecutionConcluded prosecutionConcluded, CallerType callerType) {
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
                        handleMissingPleaAndVerdict(offence, prosecutionConcluded, callerType, offenceOutcomeList);
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

    private void handleMissingPleaAndVerdict(OffenceSummary offence,
                                             ProsecutionConcluded prosecutionConcluded,
                                             CallerType callerType,
                                             List<String> offenceOutcomeList) {

        List<Result> results = offence.getResults();

        boolean hasResults = results != null && !results.isEmpty();

        boolean isConvictedPresent = hasResults && results.stream()
                .anyMatch(result -> result.getIsConvictedResult() != null);

        boolean isConvicted = isConvictedPresent && results.stream()
                .anyMatch(result -> Boolean.TRUE.equals(result.getIsConvictedResult()));

        if (QUEUE.equals(callerType) && !isConvictedPresent) {
            prosecutionConcludedDataService.execute(prosecutionConcluded);
            offenceOutcomeList.add(CrownCourtTrialOutcome.AQUITTED.getValue());
        } else {
            offenceOutcomeList.add(
                    isConvicted
                            ? CrownCourtTrialOutcome.CONVICTED.getValue()
                            : CrownCourtTrialOutcome.AQUITTED.getValue()
            );
        }
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
