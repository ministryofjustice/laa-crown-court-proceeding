package uk.gov.justice.laa.crime.crowncourt.builder;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SoftAssertionsExtension.class)
class OutcomeDTOBuilderTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Test
    void givenANullCrownCourtOutcome_whenBuildIsInvoked_thenReturnNull() {
        CrownCourtDTO dto = TestModelDataBuilder.getCrownCourtDTO();
        dto.getCrownCourtSummary().setCrownCourtOutcome(null);
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = OutcomeDTOBuilder.build(dto);
        assertThat(repOrderCCOutcomeDTOList).isNull();
    }

    @Test
    void givenAEmptyCrownCourtOutcome_whenBuildIsInvoked_thenReturnNull() {
        CrownCourtDTO dto = TestModelDataBuilder.getCrownCourtDTO();
        dto.getCrownCourtSummary().setCrownCourtOutcome(List.of());
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = OutcomeDTOBuilder.build(dto);
        assertThat(repOrderCCOutcomeDTOList).isNull();
    }

    @Test
    void givenAValidCrownCourtOutcome_whenBuildIsInvoked_thenReturnOutcome() {
        CrownCourtDTO dto = TestModelDataBuilder.getCrownCourtDTO();
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = OutcomeDTOBuilder.build(dto);
        softly.assertThat(repOrderCCOutcomeDTOList.isEmpty()).isFalse();
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getRepId())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ID);
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcome())
                .isEqualTo(dto.getCrownCourtSummary().getCrownCourtOutcome().get(0).getOutcome().getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcomeDate())
                .isEqualTo(dto.getCrownCourtSummary().getCrownCourtOutcome().get(0).getDateSet());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getUserCreated())
                .isEqualTo(dto.getUserSession().getUserName());
        softly.assertAll();
    }

    @Test
    void givenAEmptyOutcomeDate_whenBuildIsInvoked_thenReturnOutcome() {
        CrownCourtDTO dto = TestModelDataBuilder.getCrownCourtDTO();
        dto.getCrownCourtSummary().setCrownCourtOutcome(List.of(
                TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.ABANDONED, TestModelDataBuilder.TEST_COMMITTAL_DATE),
                TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.ABANDONED, null)));
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = OutcomeDTOBuilder.build(dto);
        softly.assertThat(repOrderCCOutcomeDTOList.isEmpty()).isFalse();
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getRepId())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ID);
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcome())
                .isEqualTo(dto.getCrownCourtSummary().getCrownCourtOutcome().get(0).getOutcome().getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcomeDate()).isNotNull();
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getUserCreated())
                .isEqualTo(dto.getUserSession().getUserName());
        softly.assertAll();
    }
}