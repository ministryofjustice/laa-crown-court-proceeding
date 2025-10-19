package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder;

import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.*;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Result;

import java.util.*;


public class ResultDTOBuilder {

    private ResultDTOBuilder() {
    }

    public static List<Result> build(HearingResultResponse hearingResultResponse, ProsecutionConcluded prosecutionConcluded, UUID offenceId) {

        return Optional.ofNullable(hearingResultResponse)
                .map(HearingResultResponse::getHearing)
                .filter(Objects::nonNull)
                .map(hearing -> extractJudicialResults(hearing, prosecutionConcluded, offenceId))
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .filter(Objects::nonNull)
                        .map(result -> Result.builder()
                                .isConvictedResult(result.getIs_convicted_result())
                                .build())
                        .toList())
                .orElse(Collections.emptyList());

    }

    public static List<JudicialResult> extractJudicialResults(Hearing hearing,
                                                              ProsecutionConcluded prosecutionConcluded,
                                                              UUID offenceId) {
        if (hearing == null || hearing.getProsecution_cases() == null) {
            return null;
        }

        return hearing.getProsecution_cases().stream()
                .filter(prosecutionCase -> isValidProsecutionCase(prosecutionCase, prosecutionConcluded.getProsecutionCaseId()))
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(Objects::nonNull)
                .filter(defendant -> defendant.getId() != null && defendant.getId().equals(prosecutionConcluded.getDefendantId()))
                .filter(defendant -> defendant.getOffences() != null)
                .flatMap(defendant -> defendant.getOffences().stream())
                .filter(offence -> offence != null && offence.getJudicial_results() != null)
                .filter(offence -> hasMatchingLaaApplication(offence, prosecutionConcluded.getMaatId()))
                .flatMap(offence -> offence.getJudicial_results().stream())
                .filter(judicialResult -> judicialResult.getIs_convicted_result() != null)
                .toList();


    }

    private static boolean hasMatchingLaaApplication(Offence offence, Integer maatId) {
        return offence.getLaa_application() != null
                && offence.getLaa_application().getReference() != null
                && offence.getLaa_application().getReference().intValue() == (maatId.intValue());
    }

    private static boolean isValidProsecutionCase(ProsecutionCase caseObj, UUID prosecutionCaseId) {
        return caseObj != null &&
                caseObj.getId() != null &&
                caseObj.getId().equals(prosecutionCaseId) &&
                caseObj.getDefendants() != null;
    }
}
