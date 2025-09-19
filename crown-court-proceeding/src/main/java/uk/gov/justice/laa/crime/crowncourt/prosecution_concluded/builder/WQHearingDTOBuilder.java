package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.HearingResultResponse;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.JudicialResult;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.Offence;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ProsecutionCase;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WQHearingDTOBuilder {

    private WQHearingDTOBuilder() {
    }

    public static WQHearingDTO build(HearingResultResponse hearingResultResponse, ProsecutionConcluded prosecutionConcluded) {

        if (Objects.isNull(hearingResultResponse) || Objects.isNull(hearingResultResponse.getHearing())
                || !isValidProsecution(hearingResultResponse, prosecutionConcluded)) {
            return null;
        }

        return WQHearingDTO.builder()
                .caseUrn(getCaseUrn(hearingResultResponse))
                .ouCourtLocation(Objects.nonNull(hearingResultResponse.getHearing().getCourt_centre()) ?
                        hearingResultResponse.getHearing().getCourt_centre().getOucode_l2_code() : null)
                .wqJurisdictionType(hearingResultResponse.getHearing().getJurisdiction_type())
                .resultCodes(getResultCode(hearingResultResponse, prosecutionConcluded))
                .build();
    }

    private static String getCaseUrn(HearingResultResponse hearingResultResponse) {

        if (Objects.nonNull(hearingResultResponse.getHearing().getProsecution_cases())) {
            return hearingResultResponse.getHearing().getProsecution_cases().stream()
                    .filter(Objects::nonNull)
                    .map(ProsecutionCase::getProsecution_case_identifier)
                    .filter(Objects::nonNull)
                    .map(caseU -> caseU.getCase_urn())
                    .findFirst().orElse(null);
        }
        return null;
    }

    private static String getResultCode(HearingResultResponse hearingResultResponse, ProsecutionConcluded prosecutionConcluded) {

        String resultCodes = null;
        if (Objects.nonNull(hearingResultResponse.getHearing().getProsecution_cases())) {
            List<String> resultCodeList = hearingResultResponse.getHearing().getProsecution_cases().stream()
                    .filter(prosecutionCases -> (Objects.nonNull(prosecutionCases)
                            && prosecutionCases.getId().equals(prosecutionConcluded.getProsecutionCaseId().toString())
                            && Objects.nonNull(prosecutionCases.getDefendants())))
                    .flatMap(defendantDTO -> defendantDTO.getDefendants().stream()
                            .filter(Objects::nonNull)
                            .filter(defendant -> Objects.nonNull(defendant.getId())
                                    && defendant.getId().equals(prosecutionConcluded.getDefendantId()))
                            .filter(offences -> Objects.nonNull(offences) && Objects.nonNull(offences.getOffences()))
                            .flatMap(offences -> offences.getOffences().stream()
                                    .filter(offence -> Objects.nonNull(offence) && Objects.nonNull(offence.getJudicial_results()))
                                    .filter(offence -> (Objects.nonNull(offence.getLaa_application())
                                            && Objects.nonNull(offence.getLaa_application().getReference())
                                            && offence.getLaa_application().getReference().equals(prosecutionConcluded.getMaatId().toString())))
                                    .map(Offence::getJudicial_results)
                                    .flatMap(judicialResults -> judicialResults.stream()
                                            .filter(judicialResult -> Objects.nonNull(judicialResult.getCjs_code()))
                                            .map(JudicialResult::getCjs_code))))
                    .toList();

            if (Objects.nonNull(resultCodeList) && !resultCodeList.isEmpty()) {
                resultCodes = resultCodeList.stream().collect(Collectors.joining(","));
            }
        }
        return resultCodes;
    }

    private static boolean isValidProsecution(HearingResultResponse hearingResultResponse, ProsecutionConcluded prosecutionConcluded) {

        if (Objects.nonNull(hearingResultResponse.getHearing().getProsecution_cases()) &&
                Objects.nonNull(prosecutionConcluded.getProsecutionCaseId())) {
            return hearingResultResponse.getHearing().getProsecution_cases().stream()
                    .filter(Objects::nonNull)
                    .anyMatch(prosecution -> prosecution.getId().equals(prosecutionConcluded.getProsecutionCaseId().toString()));
        }

        return false;
    }

}
