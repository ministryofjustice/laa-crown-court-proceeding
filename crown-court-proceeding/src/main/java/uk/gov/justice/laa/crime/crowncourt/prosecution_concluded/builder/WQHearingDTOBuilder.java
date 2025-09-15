package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder;

import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.HearingResultResponse;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class WQHearingDTOBuilder {

    private WQHearingDTOBuilder() {}

    public static WQHearingDTO build(HearingResultResponse hearingResultResponse, ProsecutionConcluded prosecutionConcluded) {

        if (Objects.isNull(hearingResultResponse) || Objects.isNull(hearingResultResponse.getHearing())) {
            return null;
        }

        return WQHearingDTO.builder()
                .caseUrn(getCaseUrn(hearingResultResponse))
                .ouCourtLocation(hearingResultResponse.getHearing().getCourt_centre().getOucode_l2_code())
                .wqJurisdictionType(hearingResultResponse.getHearing().getJurisdiction_type())
                .resultCodes(getResultCode(hearingResultResponse, prosecutionConcluded))
                .build();
    }

    static String getCaseUrn(HearingResultResponse hearingResultResponse) {

        if (Objects.nonNull(hearingResultResponse.getHearing().getProsecution_cases())) {
            return hearingResultResponse.getHearing().getProsecution_cases().stream()
                    .filter(prosecutionCase -> Objects.nonNull(prosecutionCase))
                    .map(caseIdentifier -> caseIdentifier.getProsecution_case_identifier())
                    .filter(caseIdentifier -> Objects.nonNull(caseIdentifier))
                    .map(caseU -> caseU.getCase_urn())
                    .findFirst().orElse(null);
        }
        return null;
    }

    static String getResultCode(HearingResultResponse hearingResultResponse, ProsecutionConcluded prosecutionConcluded) {

        String resultCodes = null;
        if (Objects.nonNull(hearingResultResponse.getHearing().getProsecution_cases())) {
            List<String> resultCodeList = hearingResultResponse.getHearing().getProsecution_cases().stream()
                    .filter(prosecutionCases -> Objects.nonNull(prosecutionCases) && Objects.nonNull(prosecutionCases.getDefendants()))
                    .flatMap(defendantDTO -> defendantDTO.getDefendants().stream()
                            .filter(defendant -> defendant.getId() == prosecutionConcluded.getDefendantId())
                            .filter(offences -> Objects.nonNull(offences) && Objects.nonNull(offences.getOffences()))
                            .flatMap(offences -> offences.getOffences().stream()
                                    .filter(offence -> Objects.nonNull(offence) && Objects.nonNull(offence.getJudicial_results()))
                                    .map(judicialResults -> judicialResults.getJudicial_results())
                                     .flatMap(judicialResults -> judicialResults.stream()
                                      .filter(judicialResult -> Objects.nonNull(judicialResult.getCjs_code()))
                                      .map(resultCode -> resultCode.getCjs_code()))))
                    .toList();

            if (Objects.nonNull(resultCodeList) && !resultCodeList.isEmpty()) {
                resultCodes = resultCodeList.stream().collect(Collectors.joining(","));
            }
        }
        return resultCodes;
    }

}
