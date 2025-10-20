package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Result;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAdapterService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.PleaTrialOutcome;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.VerdictTrialOutcome;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalculateOutcomeHelper {

    private final ProsecutionConcludedDataService prosecutionConcludedDataService;
    private final CourtDataAdapterService courtDataAdapterService;

    public String calculate(List<OffenceSummary> offenceSummaryList, ProsecutionConcluded prosecutionConcluded) {
        List<String> outcomes = buildOffenceOutComes(offenceSummaryList, prosecutionConcluded);

        log.info("Offence count: " + outcomes.size());
        return outcomes.size() == 1 ? outcomes.get(0) : CrownCourtTrialOutcome.PART_CONVICTED.getValue();
    }

    private List<String> buildOffenceOutComes(List<OffenceSummary> offenceSummaryList, ProsecutionConcluded prosecutionConcluded) {
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
                        determineOffenceOutcomeIfMissing(offence, prosecutionConcluded, offenceOutcomeList);
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

    private void determineOffenceOutcomeIfMissing(OffenceSummary offence,
                                             ProsecutionConcluded prosecutionConcluded,
                                             List<String> offenceOutcomeList) {
        List<Result> results = offence.getJudicialResults();

        if (!hasConvictionResult(results)) {
            results = courtDataAdapterService.getHearingResult(prosecutionConcluded, offence.getOffenceId());
        }

        boolean isConvicted = isConvicted(results);
        offenceOutcomeList.add(getOutcomeValue(isConvicted));
    }

    private boolean hasConvictionResult(List<Result> results) {
        return results != null
                && !results.isEmpty()
                && results.stream().anyMatch(r -> r.getIsConvictedResult() != null);
    }

    private boolean isConvicted(List<Result> results) {
        return results != null
                && !results.isEmpty()
                && results.stream().anyMatch(r -> Boolean.TRUE.equals(r.getIsConvictedResult()));
    }

    private String getOutcomeValue(boolean isConvicted) {
        return isConvicted
                ? CrownCourtTrialOutcome.CONVICTED.getValue()
                : CrownCourtTrialOutcome.AQUITTED.getValue();
    }

    /**
     * Check if both plea and verdict are missing, and no conviction result exists
     */
    public boolean isOutcomePresentWhenPleaAndVerdictEmpty(ProsecutionConcluded prosecutionConcluded) {

        if (Objects.isNull(prosecutionConcluded.getOffenceSummary()) || prosecutionConcluded.getOffenceSummary().isEmpty()) {
            return true;
        }

        return prosecutionConcluded.getOffenceSummary().stream()
                .filter(Objects::nonNull)
                .anyMatch(offenceSummary ->
                        isPleaAvailable(offenceSummary)
                                || isVerdictAvailable(offenceSummary)
                                || hasConvictionResult(offenceSummary.getJudicialResults())
                                || hasConvictionPresentInHearing(prosecutionConcluded, offenceSummary));
    }


    private boolean hasConvictionPresentInHearing(ProsecutionConcluded prosecutionConcluded, OffenceSummary offenceSummary) {
        List<Result> results = courtDataAdapterService.getHearingResult(
                prosecutionConcluded, offenceSummary.getOffenceId());
        return hasConvictionResult(results);
    }

    private boolean isPleaAvailable(OffenceSummary offenceSummary) {
        return (offenceSummary.getPlea() != null && offenceSummary.getPlea().getValue() != null);
    }

}
