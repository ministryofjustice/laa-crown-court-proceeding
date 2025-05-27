package uk.gov.justice.laa.crime.crowncourt.builder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.enums.CrownCourtOutcome;

@ExtendWith(SoftAssertionsExtension.class)
class UpdateApiResponseBuilderTest {

    @InjectSoftAssertions private SoftAssertions softly;

    @Test
    void givenAValidInput_whenBuildIsInvoked_thenReturnApiUpdateCrownCourtOutcomeResponse() {

        RepOrderDTO repOrderDTO = TestModelDataBuilder.getRepOrderDTO();
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList =
                List.of(
                        TestModelDataBuilder.getRepOrderCCOutcomeDTO(
                                3,
                                CrownCourtOutcome.PART_CONVICTED.getCode(),
                                LocalDateTime.of(2022, 2, 7, 9, 1, 25)));
        ApiUpdateCrownCourtOutcomeResponse response =
                UpdateApiResponseBuilder.build(repOrderDTO, repOrderCCOutcomeList);
        ApiCrownCourtSummary crownCourtSummary = response.getCrownCourtSummary();
        softly.assertThat(response.getModifiedDateTime()).isEqualTo(repOrderDTO.getDateModified());
        softly.assertThat(crownCourtSummary.getRepOrderDate())
                .isEqualTo(repOrderDTO.getCrownRepOrderDate().atStartOfDay());
        softly.assertThat(crownCourtSummary.getRepOrderDecision())
                .isEqualTo(repOrderDTO.getCrownRepOrderDecision());
        softly.assertThat(crownCourtSummary.getRepType())
                .isEqualTo(repOrderDTO.getCrownRepOrderType());
        softly.assertThat(crownCourtSummary.getEvidenceFeeLevel())
                .isEqualTo(repOrderDTO.getEvidenceFeeLevel());
        softly.assertThat(
                        crownCourtSummary
                                .getRepOrderCrownCourtOutcome()
                                .get(0)
                                .getOutcome()
                                .getCode())
                .isEqualTo(repOrderCCOutcomeList.get(0).getOutcome());
        softly.assertThat(crownCourtSummary.getRepOrderCrownCourtOutcome().get(0).getOutcomeDate())
                .isEqualTo(repOrderCCOutcomeList.get(0).getOutcomeDate());
        softly.assertAll();
    }

    @Test
    void givenARepOrderIsEmpty_whenBuildIsInvoked_thenReturnApiUpdateCrownCourtOutcomeResponse() {

        RepOrderDTO repOrderDTO = TestModelDataBuilder.getRepOrderDTO();
        ApiUpdateCrownCourtOutcomeResponse response =
                UpdateApiResponseBuilder.build(repOrderDTO, Collections.emptyList());
        ApiCrownCourtSummary crownCourtSummary = response.getCrownCourtSummary();
        softly.assertThat(response.getModifiedDateTime()).isEqualTo(repOrderDTO.getDateModified());
        softly.assertThat(crownCourtSummary.getRepOrderDate())
                .isEqualTo(repOrderDTO.getCrownRepOrderDate().atStartOfDay());
        softly.assertThat(crownCourtSummary.getRepOrderDecision())
                .isEqualTo(repOrderDTO.getCrownRepOrderDecision());
        softly.assertThat(crownCourtSummary.getRepType())
                .isEqualTo(repOrderDTO.getCrownRepOrderType());
        softly.assertThat(crownCourtSummary.getEvidenceFeeLevel())
                .isEqualTo(repOrderDTO.getEvidenceFeeLevel());
        softly.assertAll();
    }
}
