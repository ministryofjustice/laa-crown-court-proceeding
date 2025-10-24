package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateCrownCourtRequest;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
class CrownCourtDTOBuilderTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    private void checkCommonFields(ApiProcessRepOrderRequest request, CrownCourtDTO dto) {
        softly.assertThat(dto.getRepId()).isEqualTo(request.getRepId());
        softly.assertThat(dto.getCaseType()).isEqualTo(request.getCaseType());
        softly.assertThat(dto.getMagCourtOutcome()).isEqualTo(request.getMagCourtOutcome());
        softly.assertThat(dto.getMagsDecisionResult().getDecisionReason()).isEqualTo(request.getDecisionReason());
        softly.assertThat(dto.getCommittalDate()).isEqualTo(request.getCommittalDate());
        softly.assertThat(dto.getDateReceived()).isEqualTo(request.getDateReceived());
        softly.assertThat(dto.getCrownCourtSummary()).isEqualTo(request.getCrownCourtSummary());
        softly.assertThat(dto.getIojSummary()).isEqualTo(request.getIojAppeal());
        softly.assertThat(dto.getFinancialAssessment()).isEqualTo(request.getFinancialAssessment());
        softly.assertThat(dto.getPassportAssessment()).isEqualTo(request.getPassportAssessment());
        softly.assertAll();
    }

    @Test
    void givenMagsDecisionRequest_whenBuildInvoked_thenCrownCourtDTOFieldsMappedCorrectly() {
        ApiProcessRepOrderRequest request = TestModelDataBuilder.getApiProcessRepOrderRequest(true);
        CrownCourtDTO dto = CrownCourtDTOBuilder.build(request);
        softly.assertThat(dto.getMagsDecisionResult().getDecisionDate())
                .isEqualTo(request.getDecisionDate().toLocalDate());
        checkCommonFields(request, dto);
    }

    @Test
    void givenApiProcessRepOrderRequest_whenBuildIsInvoked_thenCorrectCrownCourtDTOFieldsArePopulated() {
        ApiProcessRepOrderRequest request = TestModelDataBuilder.getApiProcessRepOrderRequest(true);
        request.setDecisionDate(null);
        CrownCourtDTO dto = CrownCourtDTOBuilder.build(request);
        softly.assertThat(dto.getMagsDecisionResult().getDecisionDate()).isNull();
        checkCommonFields(request, dto);
    }

    @Test
    void givenApiUpdateApplicationRequest_whenBuildIsInvoked_thenCorrectCrownCourtDTOFieldsArePopulated() {
        ApiUpdateApplicationRequest request = TestModelDataBuilder.getApiUpdateApplicationRequest(true);
        CrownCourtDTO dto = CrownCourtDTOBuilder.build(request);

        checkCommonFields(request, dto);

        softly.assertThat(dto.getMagsDecisionResult().getDecisionDate())
                .isEqualTo(request.getDecisionDate().toLocalDate());
        softly.assertThat(dto.getUserSession()).isEqualTo(request.getUserSession());
        softly.assertThat(dto.getCrownRepId()).isEqualTo(request.getCrownRepId());
        softly.assertThat(dto.getApplicantHistoryId()).isEqualTo(request.getApplicantHistoryId());
        softly.assertThat(dto.getIsImprisoned()).isEqualTo(request.getIsImprisoned());
        softly.assertAll();
    }

    @Test
    void givenApiUpdateCrownCourtRequest_whenBuildIsInvoked_thenCorrectCrownCourtDTOFieldsArePopulated() {
        ApiUpdateCrownCourtRequest request = TestModelDataBuilder.getApiUpdateCrownCourtRequest(true);
        CrownCourtDTO dto = CrownCourtDTOBuilder.build(request);
        checkCommonFields(request, dto);

        softly.assertThat(dto.getMagsDecisionResult().getDecisionDate())
                .isEqualTo(request.getDecisionDate().toLocalDate());
        softly.assertThat(dto.getUserSession()).isEqualTo(request.getUserSession());
        softly.assertThat(dto.getCrownRepId()).isEqualTo(request.getCrownRepId());
        softly.assertThat(dto.getApplicantHistoryId()).isEqualTo(request.getApplicantHistoryId());
        softly.assertThat(dto.getIsImprisoned()).isEqualTo(request.getIsImprisoned());

        softly.assertThat(dto.getCapitalEvidence())
                .isEqualTo(List.of(
                        TestModelDataBuilder.getCapitalEvidenceDTO(TestModelDataBuilder.TEST_DATE_RECEIVED, "Type")));
        softly.assertThat(dto.getIncomeEvidenceReceivedDate()).isEqualTo(TestModelDataBuilder.INCOME_EVIDENCE_DATE);
        softly.assertThat(dto.getCapitalEvidenceReceivedDate()).isEqualTo(TestModelDataBuilder.CAPITAL_EVIDENCE_DATE);
        softly.assertThat(dto.getEmstCode()).isEqualTo(TestModelDataBuilder.EMST_CODE);

        softly.assertAll();
    }
}
