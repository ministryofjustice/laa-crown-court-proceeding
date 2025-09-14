package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.enums.CallerType;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.*;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.enums.CallerType.QUEUE;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.enums.CallerType.SCHEDULER;

@ExtendWith(MockitoExtension.class)
class CalculateOutcomeHelperTest {

    @InjectMocks
    private CalculateOutcomeHelper calculateOutcomeHelper;

    CallerType callerTypeQueue = QUEUE;
    CallerType callerTypeScheduler = SCHEDULER;

    @Mock
    private ProsecutionConcludedDataService prosecutionConcludedDataService;

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
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
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
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
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
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
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
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
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
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceived_whenPleaValueIsEmpty_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .plea(Plea.builder().build())
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceived_whenPleaIsEmpty_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceived_whenOffenceSummaryIsEmpty_thenReturnPartConvicted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of())
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
        assertThat(res).isEqualTo("PART CONVICTED");
    }

    @Test
    void givenMessageIsReceived_whenPleaIsGuilty_thenReturnOutcomeAsConvicted() {
        ProsecutionConcluded prosecutionConcluded = getProsecutionConcluded();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
        assertThat(res).isEqualTo("CONVICTED");
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

    @Test
    void givenMessageIsReceivedFromQueue_whenPleaAndVerdictIsNotAvailable_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .plea(Plea.builder().build())
                                .verdict(Verdict.builder().build())
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceivedFromQueue_whenPleaAndVerdictIsNotAvailable_NotConvictedResult_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .plea(Plea.builder().build())
                                .verdict(Verdict.builder().build())
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeScheduler);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceivedFromQueue_whenPleaAndVerdictIsNotAvailable_ConvictedResult_thenReturnOutcomeAsConvicted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .plea(Plea.builder().build())
                                .verdict(Verdict.builder().build())
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeQueue);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceivedFromScheduler_whenPleaAndVerdictIsNotAvailable_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .plea(Plea.builder().build())
                                .verdict(Verdict.builder().build())
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeScheduler);
        assertThat(res).isEqualTo("AQUITTED");
    }

    @Test
    void givenMessageIsReceivedFromScheduler_whenPleaAndVerdictIsNotAvailable_NotConvictedResult_thenReturnOutcomeAsAquitted() {
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .plea(Plea.builder().build())
                                .verdict(Verdict.builder().build())
                                .build()
                ))
                .build();
        String res = calculateOutcomeHelper.calculate(prosecutionConcluded.getOffenceSummary(), prosecutionConcluded, callerTypeScheduler);
        assertThat(res).isEqualTo("AQUITTED");
    }
}