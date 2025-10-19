package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.*;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAdapterService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculateOutcomeHelperTest {

    @InjectMocks
    private CalculateOutcomeHelper calculateOutcomeHelper;

    @Mock
    private ProsecutionConcludedDataService prosecutionConcludedDataService;

    @Mock
    private CourtDataAdapterService courtDataAdapterService;

    @Test
    void givenMessageIsReceived_whenPleaAndVerdictIsAvailable_thenReturnOutcomeAsPartConvicted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .verdict(getVerdict("GUILTY"))
                                .plea(Plea.builder().value("NOT_GUILTY").pleaDate("2021-11-12").build())
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("CONVICTED");
    }

    @Test
    void givenMessageIsReceived_whenMultipleOffenceIsAvailable_thenReturnOutcomeAsPartConvicted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(
                        Arrays.asList(
                                OffenceSummary.builder()
                                        .verdict(getVerdict("GUILTY"))
                                        .plea(Plea.builder().value("NOT_GUILTY").pleaDate("2021-11-12").build())
                                        .build(),
                                OffenceSummary.builder()
                                        .verdict(getVerdict("NOT GUILTY"))
                                        .plea(Plea.builder().value("GUILTY").pleaDate("2021-11-12").build())
                                        .build()
                        ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("PART CONVICTED");
    }

    @Test
    void givenMessageIsReceived_whenVerdictIsGuilty_thenReturnOutcomeAsConvicted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .verdict(getVerdict("GUILTY"))
                                .plea(Plea.builder().value("NOT_GUILTY").pleaDate("2025-10-11").build())
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("CONVICTED");
    }

    @Test
    void givenMessageIsReceived_whenVerdictIsNotGuilty_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .verdict(getVerdict("NOT_GUILTY"))
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("AQUITTED");
    }

    private Verdict getVerdict(String verdictType) {
        return Verdict.builder()
                .verdictType(VerdictType.builder().categoryType(verdictType).build())
                .verdictDate("2021-11-12")
                .build();
    }

    @Test
    void givenMessageIsReceived_whenPleaIsNotGuilty_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .plea(Plea.builder().value("NOT_GUILTY").pleaDate("2021-11-12").build())
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceived_whenOffenceSummaryIsEmpty_thenReturnPartConvicted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of())
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("PART CONVICTED");
    }

    @Test
    void givenMessageIsReceived_whenPleaIsGuilty_thenReturnOutcomeAsConvicted() {
        ProsecutionConcluded prosecutionConcluded = getProsecutionConcluded();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("CONVICTED");
    }

    @ParameterizedTest
    @MethodSource("prosecutionConcludedForOutcome")
    void givenAProsecutionConcluded_whenIsOutcomePresentWhenPleaAndVerdictEmpty_thenCorrectResultReturned(ProsecutionConcluded prosecutionConcluded,
                                                                                                          boolean expectedResult,
                                                                                                          boolean shouldCallCda,
                                                                                                          List<Result> expectedResultList) {
        if (shouldCallCda) {
            when(courtDataAdapterService.getHearingResult(any(), any(UUID.class))).thenReturn(expectedResultList);
        }
        boolean result = calculateOutcomeHelper.isOutcomePresentWhenPleaAndVerdictEmpty(prosecutionConcluded);
        assertThat(result).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> prosecutionConcludedForOutcome() {
        return Stream.of(
                Arguments.of(TestModelDataBuilder.getProsecutionConcluded(false,false,false,false), true, false, null),
                Arguments.of(TestModelDataBuilder.getProsecutionConcluded(true,true,false,false), true, false, null),
                Arguments.of(TestModelDataBuilder.getProsecutionConcluded(true,false,true,false), true, false, null),
                Arguments.of(TestModelDataBuilder.getProsecutionConcluded(true,false,false,true), true, false, null),
                Arguments.of(TestModelDataBuilder.getProsecutionConcluded(true,false,false,false), false, true, null),
                Arguments.of(TestModelDataBuilder.getProsecutionConcluded(true,false,false,false), false, true, Collections.emptyList()),
                Arguments.of(TestModelDataBuilder.getProsecutionConcluded(true,false,false,false), true, true, List.of(Result.builder().isConvictedResult(Boolean.TRUE).build()))
        );
    }

    @ParameterizedTest
    @MethodSource("emptyPleaAndVerdict")
    void givenAEmptyPleaAndVerdict_whenCalculateIsInvoked_thenCorrectResultReturned(List<OffenceSummary> offenceList,
                                                                                    String expectedResult,
                                                                                    boolean shouldCallCda,
                                                                                    List<Result> expectedResultList) {
        if (shouldCallCda) {
            when(courtDataAdapterService.getHearingResult(any(), any(UUID.class))).thenReturn(expectedResultList);
        }
        String result = calculateOutcomeHelper.calculate(offenceList, getProsecutionConcluded());
        assertThat(result).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> emptyPleaAndVerdict() {
        OffenceSummary emptyConvicted = TestModelDataBuilder.getOffenceSummary(true,false);
        emptyConvicted.getJudicialResults().get(0).setIsConvictedResult(null);
        return Stream.of(
               Arguments.of(List.of(TestModelDataBuilder.getOffenceSummary(true,false)),
                        CrownCourtTrialOutcome.AQUITTED.getValue(), false, null),
                Arguments.of(List.of(TestModelDataBuilder.getOffenceSummary(true,true)),
                        CrownCourtTrialOutcome.CONVICTED.getValue(), false, null),
                Arguments.of(List.of(TestModelDataBuilder.getOffenceSummary(false,false)),
                        CrownCourtTrialOutcome.AQUITTED.getValue(), true, List.of(Result.builder().isConvictedResult(false).build())),
                Arguments.of(List.of(TestModelDataBuilder.getOffenceSummary(false,false)),
                        CrownCourtTrialOutcome.CONVICTED.getValue(), true, List.of(Result.builder().isConvictedResult(true).build())),
                Arguments.of(List.of(emptyConvicted),
                        CrownCourtTrialOutcome.CONVICTED.getValue(), true, List.of(Result.builder().isConvictedResult(true).build())),
                Arguments.of(List.of(emptyConvicted),
                        CrownCourtTrialOutcome.AQUITTED.getValue(), true, List.of(Result.builder().isConvictedResult(false).build()))
                );
    }

    private ProsecutionConcluded getProsecutionConcluded() {
        return ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .plea(Plea.builder().value("GUILTY").pleaDate("2021-12-12").build())
                                .build()
                ))
                .build();
    }
}