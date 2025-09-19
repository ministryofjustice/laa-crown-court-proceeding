package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder;

import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.HearingResultResponse;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class WQHearingDTOBuilderTest {

    @InjectMocks
    private WQHearingDTOBuilder wqHearingDTOBuilder;



    @ParameterizedTest
    @MethodSource("getHearingResultScenarios")
    void givenAHearingResult_whenBuildIsInvoked_thenReturnWQHearingDTO(HearingResultResponse hearingResult,
                                                                       ProsecutionConcluded prosecutionConcluded,
                                                                       WQHearingDTO wqHearingDTO) {
        WQHearingDTO wqHearing = wqHearingDTOBuilder.build(hearingResult, prosecutionConcluded);
        assertThat(wqHearing).isEqualTo(wqHearingDTO);
    }



    private static Stream<Arguments> getHearingResultScenarios() {

        HearingResultResponse emptyProsecutionCase = TestModelDataBuilder.getHearingResultResponse(false,false,false);
        emptyProsecutionCase.getHearing().setProsecution_cases(null);

        return Stream.of(
                Arguments.of(HearingResultResponse.builder().build(), ProsecutionConcluded.builder().build(),null),

                Arguments.of(TestModelDataBuilder.getHearingResultResponse(true,true,true),
                        getProsecutionConcluded(),TestModelDataBuilder.getWqHearingDTO(TestModelDataBuilder.RESULT_CODE)),

                 Arguments.of(TestModelDataBuilder.getHearingResultResponse(true,true,false),
                        getProsecutionConcluded(),TestModelDataBuilder.getWqHearingDTO(null)),

                Arguments.of(TestModelDataBuilder.getHearingResultResponse(true,false,false),
                        getProsecutionConcluded(),TestModelDataBuilder.getWqHearingDTO(null)),

                Arguments.of(TestModelDataBuilder.getHearingResultResponse(false,false,false),
                        getProsecutionConcluded(),TestModelDataBuilder.getWqHearingDTO(null)),

                Arguments.of(emptyProsecutionCase,getProsecutionConcluded(),null)

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