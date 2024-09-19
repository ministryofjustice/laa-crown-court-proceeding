package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class CaseConclusionDTOBuilderTest {

    @InjectMocks
    private CaseConclusionDTOBuilder caseConclusionDTOBuilder;

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Test
    void givenANullOffenceSummary_whenGetMostRecentCaseEndDateIsInvoked_thenNullIsReturn() {
        assertThat(caseConclusionDTOBuilder.getMostRecentCaseEndDate(null)).isNull();
    }

    @Test
    void givenAEmptyOffenceSummary_whenGetMostRecentCaseEndDateIsInvoked_thenNullIsReturn() {
        assertThat(caseConclusionDTOBuilder.getMostRecentCaseEndDate(List.of())).isNull();
    }

    @Test
    void givenANullChangeDate_whenGetMostRecentCaseEndDateIsInvoked_thenNullIsReturn() {

        List<OffenceSummary> offenceSummaryList = new ArrayList<>();
        offenceSummaryList.add(TestModelDataBuilder.getOffenceSummary(UUID.randomUUID(), null));
        assertThat(caseConclusionDTOBuilder.getMostRecentCaseEndDate(offenceSummaryList)).isNull();
    }

    @Test
    void givenAEmptyChangeDate_whenGetMostRecentCaseEndDateIsInvoked_thenNullIsReturn() {

        List<OffenceSummary> offenceSummaryList = new ArrayList<>();
        offenceSummaryList.add(TestModelDataBuilder.getOffenceSummary(UUID.randomUUID(), ""));
        assertThat(caseConclusionDTOBuilder.getMostRecentCaseEndDate(offenceSummaryList)).isNull();
    }

    @Test
    void givenAValidOffenceSummary_whenGetMostRecentCaseEndDateIsInvoked_thenCorrectDateIsReturn() {

        List<OffenceSummary> offenceSummaryList = new ArrayList<>();
        offenceSummaryList.add(TestModelDataBuilder.getOffenceSummary(UUID.randomUUID(), "2023-01-01"));
        offenceSummaryList.add(TestModelDataBuilder.getOffenceSummary(UUID.randomUUID(), "2022-02-02"));
        assertThat(caseConclusionDTOBuilder.getMostRecentCaseEndDate(offenceSummaryList)).isEqualTo("2023-01-01");
    }

    @Test
    void givenANullResultCode_whenBuildResultCodeListIsInvoked_thenEmptyIsReturn() {
        assertThat(caseConclusionDTOBuilder.buildResultCodeList(WQHearingDTO.builder().build())).isEqualTo(List.of(""));
    }

    @Test
    void givenAValidResultCode_whenBuildResultCodeListIsInvoked_thenCorrectValueIsReturn() {
        assertThat(caseConclusionDTOBuilder.buildResultCodeList(WQHearingDTO.builder().resultCodes("code1,code2,code3").build()))
                .isEqualTo(List.of("code1","code2","code3"));
    }

    @Test
    void givenAValidProsecutionConcluded_whenBuildIsInvoked_thenReturnConcludedDTO() {
        ConcludedDTO concludedDTO = caseConclusionDTOBuilder.build(TestModelDataBuilder.getProsecutionConcluded(),
                TestModelDataBuilder.getWQHearingDTO(), "2022-01-01");

        softly.assertThat(concludedDTO.getCalculatedOutcome()).isEqualTo("2022-01-01");
        softly.assertThat(concludedDTO.getOuCourtLocation()).isEqualTo("loc1");
        softly.assertThat(concludedDTO.getWqJurisdictionType()).isEqualTo("Type");
        softly.assertThat(concludedDTO.getCaseUrn()).isEqualTo("45673");
        softly.assertThat(concludedDTO.getCaseEndDate()).isEqualTo("2021-11-12");
        softly.assertThat(concludedDTO.getHearingResultCodeList()).isEqualTo(List.of("code1", "code2", "code3"));
        softly.assertThat(concludedDTO.getProsecutionConcluded()).isEqualTo(TestModelDataBuilder.getProsecutionConcluded());
        softly.assertAll();
    }

}