package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.HearingResultResponse;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Result;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ResultDTOBuilderTest {

    @InjectMocks
    private ResultDTOBuilder resultDTOBuilder;



    @ParameterizedTest
    @MethodSource("getHearingResultScenarios")
    void givenAHearingResult_whenBuildIsInvoked_thenReturnWQHearingDTO(HearingResultResponse hearingResult,
                                                                       ProsecutionConcluded prosecutionConcluded,
                                                                       List<Result> expectedResult) {
        List<Result> results = resultDTOBuilder.build(hearingResult, prosecutionConcluded, TestModelDataBuilder.OFFENCE_ID);
        assertThat(results).isEqualTo(expectedResult);

    }

    private static Stream<Arguments> getHearingResultScenarios() {

        HearingResultResponse emptyProsecutionCase = TestModelDataBuilder.getHearingResultResponse(false,false,false);
        emptyProsecutionCase.getHearing().setProsecution_cases(null);

        HearingResultResponse invalidProsecutionCase = TestModelDataBuilder.getHearingResultResponse(false,false,false);
        invalidProsecutionCase.getHearing().getProsecution_cases().get(0).setId(UUID.randomUUID());

        HearingResultResponse invalidDefendant = TestModelDataBuilder.getHearingResultResponse(true,false,false);
        invalidDefendant.getHearing().getProsecution_cases().get(0).getDefendants().get(0).setId(UUID.randomUUID());

        HearingResultResponse emptyDefendant = TestModelDataBuilder.getHearingResultResponse(true,false,false);
        invalidDefendant.getHearing().getProsecution_cases().get(0).getDefendants().get(0).setId(null);


        HearingResultResponse invalidOffence = TestModelDataBuilder.getHearingResultResponse(true,true,false);
        invalidOffence.getHearing().getProsecution_cases().get(0).getDefendants().get(0).getOffences().get(0).setId(UUID.randomUUID());

        HearingResultResponse emptyOffence = TestModelDataBuilder.getHearingResultResponse(true,true,false);
        emptyOffence.getHearing().getProsecution_cases().get(0).getDefendants().get(0).getOffences().get(0).setId(null);

        HearingResultResponse emptyResult = TestModelDataBuilder.getHearingResultResponse(true,true,true);
        emptyResult.getHearing().getProsecution_cases().get(0).getDefendants().get(0).getOffences().get(0).getJudicial_results().get(0).setIs_convicted_result(null);


        return Stream.of(
                Arguments.of(null, getProsecutionConcluded(), Collections.emptyList()),

                Arguments.of(HearingResultResponse.builder().build(), ProsecutionConcluded.builder().build(), Collections.emptyList()),

                Arguments.of(TestModelDataBuilder.getHearingResultResponse(true,false,false),
                        getProsecutionConcluded(), Collections.emptyList()),

                Arguments.of(TestModelDataBuilder.getHearingResultResponse(true,true,false),
                        getProsecutionConcluded(), Collections.emptyList()),

                Arguments.of(emptyProsecutionCase,getProsecutionConcluded(), Collections.emptyList()),
                Arguments.of(invalidProsecutionCase,getProsecutionConcluded(), Collections.emptyList()),
                Arguments.of(invalidDefendant,getProsecutionConcluded(), Collections.emptyList()),
                Arguments.of(emptyDefendant,getProsecutionConcluded(), Collections.emptyList()),
                Arguments.of(invalidOffence,getProsecutionConcluded(), Collections.emptyList()),
                Arguments.of(emptyOffence,getProsecutionConcluded(), Collections.emptyList()),
                Arguments.of(emptyResult,getProsecutionConcluded(), Collections.emptyList()),

                Arguments.of(TestModelDataBuilder.getHearingResultResponse(true,true,true),
                        getProsecutionConcluded(), List.of(Result.builder().isConvictedResult(Boolean.TRUE).build()))

        );
    }

    private static ProsecutionConcluded getProsecutionConcluded() {
        return ProsecutionConcluded.builder()
                .defendantId(TestModelDataBuilder.DEFENDANT_ID)
                .prosecutionCaseId(TestModelDataBuilder.PROSECUTION_CASE_ID)
                .maatId(TestModelDataBuilder.TEST_REP_ID)
                .build();
    }

}