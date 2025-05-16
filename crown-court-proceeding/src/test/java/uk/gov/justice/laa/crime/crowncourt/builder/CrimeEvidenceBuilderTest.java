package uk.gov.justice.laa.crime.crowncourt.builder;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiCalculateEvidenceFeeRequest;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;

@ExtendWith(SoftAssertionsExtension.class)
class CrimeEvidenceBuilderTest {

    @InjectSoftAssertions private SoftAssertions softly;

    @Test
    void givenAValidCrownCourtInput_whenBuildIsInvoked_thenReturnApiCalculateEvidenceFeeRequest() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        ApiCalculateEvidenceFeeRequest request = CrimeEvidenceBuilder.build(crownCourtDTO);
        softly.assertThat(request.getRepId()).isEqualTo(crownCourtDTO.getRepId());
        softly.assertThat(request.getMagCourtOutcome())
                .isEqualTo(crownCourtDTO.getMagCourtOutcome().getOutcome());
        softly.assertThat(request.getEvidenceFee().getFeeLevel())
                .isEqualTo(crownCourtDTO.getEvidenceFeeLevel().getFeeLevel());
        softly.assertThat(request.getEvidenceFee().getDescription())
                .isEqualTo(crownCourtDTO.getEvidenceFeeLevel().getDescription());
        softly.assertThat(request.getCapitalEvidence().get(0).getDateReceived())
                .isEqualTo(crownCourtDTO.getCapitalEvidence().get(0).getDateReceived());
        softly.assertThat(request.getCapitalEvidence().get(0).getEvidenceType())
                .isEqualTo(crownCourtDTO.getCapitalEvidence().get(0).getEvidenceType());
        softly.assertThat(request.getIncomeEvidenceReceivedDate())
                .isEqualTo(crownCourtDTO.getIncomeEvidenceReceivedDate());
        softly.assertThat(request.getCapitalEvidenceReceivedDate())
                .isEqualTo(crownCourtDTO.getCapitalEvidenceReceivedDate());
        softly.assertAll();
    }

    @Test
    void
            givenAEvidenceFeeAndCapitalEvidenceAsNull_whenBuildIsInvoked_thenReturnApiCalculateEvidenceFeeRequest() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtDTO.setCapitalEvidence(null);
        crownCourtDTO.setEvidenceFeeLevel(null);
        ApiCalculateEvidenceFeeRequest request = CrimeEvidenceBuilder.build(crownCourtDTO);
        softly.assertThat(request.getRepId()).isEqualTo(crownCourtDTO.getRepId());
        softly.assertThat(request.getMagCourtOutcome())
                .isEqualTo(crownCourtDTO.getMagCourtOutcome().getOutcome());
        softly.assertThat(request.getEvidenceFee()).isNull();
        softly.assertThat(request.getCapitalEvidence()).isEmpty();
        softly.assertThat(request.getIncomeEvidenceReceivedDate())
                .isEqualTo(crownCourtDTO.getIncomeEvidenceReceivedDate());
        softly.assertThat(request.getCapitalEvidenceReceivedDate())
                .isEqualTo(crownCourtDTO.getCapitalEvidenceReceivedDate());
        softly.assertAll();
    }
}
