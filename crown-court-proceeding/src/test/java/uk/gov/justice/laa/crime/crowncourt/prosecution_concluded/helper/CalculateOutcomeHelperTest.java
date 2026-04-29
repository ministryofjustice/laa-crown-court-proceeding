package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Plea;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Result;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Verdict;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.VerdictType;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAdapterService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalculateOutcomeHelperTest {

    @InjectMocks
    private CalculateOutcomeHelper calculateOutcomeHelper;

    @Mock
    private CourtDataAdapterService courtDataAdapterService;

    @Mock
    private ProsecutionConcludedValidator prosecutionConcludedValidator;

    @Test
    void givenMessageIsReceived_whenPleaAndVerdictIsAvailable_thenReturnOutcomeAsPartConvicted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(OffenceSummary.builder()
                        .offenceCode("1212")
                        .verdict(getVerdict("GUILTY"))
                        .plea(Plea.builder()
                                .value("NOT_GUILTY")
                                .pleaDate("2021-11-12")
                                .build())
                        .build()))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("CONVICTED");
    }

    @Test
    void givenMessageIsReceived_whenMultipleOffenceIsAvailable_thenReturnOutcomeAsPartConvicted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(Arrays.asList(
                        OffenceSummary.builder()
                                .verdict(getVerdict("GUILTY"))
                                .plea(Plea.builder()
                                        .value("NOT_GUILTY")
                                        .pleaDate("2021-11-12")
                                        .build())
                                .build(),
                        OffenceSummary.builder()
                                .verdict(getVerdict("NOT GUILTY"))
                                .plea(Plea.builder()
                                        .value("GUILTY")
                                        .pleaDate("2021-11-12")
                                        .build())
                                .build()))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("PART CONVICTED");
    }

    @Test
    void givenMessageIsReceived_whenVerdictIsGuilty_thenReturnOutcomeAsConvicted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(OffenceSummary.builder()
                        .offenceCode("1212")
                        .verdict(getVerdict("GUILTY"))
                        .plea(Plea.builder()
                                .value("NOT_GUILTY")
                                .pleaDate("2025-10-11")
                                .build())
                        .build()))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("CONVICTED");
    }

    @Test
    void givenMessageIsReceived_whenVerdictIsNotGuilty_thenReturnOutcomeAsAcquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(OffenceSummary.builder()
                        .offenceCode("1212")
                        .verdict(getVerdict("NOT_GUILTY"))
                        .build()))
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
    void givenMessageIsReceived_whenPleaIsNotGuilty_thenReturnOutcomeAsAcquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(OffenceSummary.builder()
                        .offenceCode("1212")
                        .plea(Plea.builder()
                                .value("NOT_GUILTY")
                                .pleaDate("2021-11-12")
                                .build())
                        .build()))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceived_whenPleaValueIsEmpty_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(OffenceSummary.builder()
                        .offenceCode("1212")
                        .offenceId(UUID.randomUUID())
                        .plea(Plea.builder().build())
                        .build()))
                .build();
        when(courtDataAdapterService.getHearingResult(any(ProsecutionConcluded.class), any(UUID.class)))
                .thenReturn(List.of(Result.builder().isConvictedResult(false).build()));
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceived_whenPleaIsEmpty_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(OffenceSummary.builder()
                        .offenceId(UUID.randomUUID())
                        .offenceCode("1212")
                        .build()))
                .build();
        when(courtDataAdapterService.getHearingResult(any(ProsecutionConcluded.class), any(UUID.class)))
                .thenReturn(List.of(Result.builder().isConvictedResult(false).build()));
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

    private ProsecutionConcluded getProsecutionConcluded() {
        return ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(OffenceSummary.builder()
                        .offenceCode("1212")
                        .plea(Plea.builder()
                                .value("GUILTY")
                                .pleaDate("2021-12-12")
                                .build())
                        .build()))
                .build();
    }

    @Test
    void givenOffenceSummary_whenPleaAndVerdictIsNotAvailable_thenReturnOutcomeAsAcquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(OffenceSummary.builder()
                        .offenceCode("1212")
                        .offenceId(UUID.randomUUID())
                        .plea(Plea.builder().build())
                        .verdict(Verdict.builder().build())
                        .build()))
                .build();
        when(courtDataAdapterService.getHearingResult(any(ProsecutionConcluded.class), any(UUID.class)))
                .thenReturn(List.of(Result.builder().isConvictedResult(false).build()));
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded);
        assertThat(res).isEqualTo("AQUITTED");
    }
}
