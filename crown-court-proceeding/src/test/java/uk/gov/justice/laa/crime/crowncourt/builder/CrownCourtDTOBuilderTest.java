package uk.gov.justice.laa.crime.crowncourt.builder;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateApplicationRequest;

@ExtendWith(SoftAssertionsExtension.class)
class CrownCourtDTOBuilderTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    private void checkCommonFields(ApiProcessRepOrderRequest request, CrownCourtDTO dto) {
        softly.assertThat(dto.getRepId())
                .isEqualTo(request.getRepId());
        softly.assertThat(dto.getCaseType())
                .isEqualTo(request.getCaseType());
        softly.assertThat(dto.getMagCourtOutcome())
                .isEqualTo(request.getMagCourtOutcome());
        softly.assertThat(dto.getDecisionReason())
                .isEqualTo(request.getDecisionReason());
        softly.assertThat(dto.getDecisionDate())
                .isEqualTo(request.getDecisionDate());
        softly.assertThat(dto.getCommittalDate())
                .isEqualTo(request.getCommittalDate());
        softly.assertThat(dto.getDateReceived())
                .isEqualTo(request.getDateReceived());
        softly.assertThat(dto.getCrownCourtSummary())
                .isEqualTo(request.getCrownCourtSummary());
        softly.assertThat(dto.getIojAppeal())
                .isEqualTo(request.getIojAppeal());
        softly.assertThat(dto.getFinancialAssessment())
                .isEqualTo(request.getFinancialAssessment());
        softly.assertThat(dto.getPassportAssessment())
                .isEqualTo(request.getPassportAssessment());
        softly.assertAll();
    }

    @Test
    void givenApiProcessRepOrderRequest_whenBuildIsInvoked_thenCorrectCrownCourtDTOFieldsArePopulated() {
        ApiProcessRepOrderRequest request = TestModelDataBuilder.getApiProcessRepOrderRequest(true);
        CrownCourtDTO dto = CrownCourtDTOBuilder.build(request);
        checkCommonFields(request, dto);
    }

    @Test
    void givenApiUpdateApplicationRequest_whenBuildIsInvoked_thenCorrectCrownCourtDTOFieldsArePopulated() {
        ApiUpdateApplicationRequest request = TestModelDataBuilder.getApiUpdateApplicationRequest(true);
        CrownCourtDTO dto = CrownCourtDTOBuilder.build(request);

        checkCommonFields(request, dto);

        softly.assertThat(dto.getUserSession())
                .isEqualTo(request.getUserSession());
        softly.assertThat(dto.getCrownRepId())
                .isEqualTo(request.getCrownRepId());
        softly.assertThat(dto.getApplicantHistoryId())
                .isEqualTo(request.getApplicantHistoryId());
        softly.assertThat(dto.getIsImprisoned())
                .isEqualTo(request.getIsImprisoned());
        softly.assertAll();
    }
}
