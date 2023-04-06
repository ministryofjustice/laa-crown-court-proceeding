package uk.gov.justice.laa.crime.crowncourt.builder;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;

@ExtendWith(SoftAssertionsExtension.class)
class UpdateRepOrderDTOBuilderTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Test
    void givenCrownCourtDTO_whenBuildIsInvoked_thenCorrectUpdateRepOrderRequestDTOFieldsArePopulated() {
        CrownCourtDTO dto = TestModelDataBuilder.getCrownCourtDTO();
        ApiProcessRepOrderResponse apiProcessRepOrderResponse = TestModelDataBuilder.getApiProcessRepOrderResponse();
        UpdateRepOrderRequestDTO updateRequest = UpdateRepOrderDTOBuilder.build(dto, apiProcessRepOrderResponse);

        softly.assertThat(updateRequest.getRepId())
                .isEqualTo(dto.getRepId());
        softly.assertThat(updateRequest.getCrownRepId())
                .isEqualTo(dto.getCrownRepId());
        softly.assertThat(updateRequest.getCrownRepOrderDecision())
                .isEqualTo(apiProcessRepOrderResponse.getRepOrderDecision());
        softly.assertThat(updateRequest.getCrownRepOrderType())
                .isEqualTo(apiProcessRepOrderResponse.getRepType());
        softly.assertThat(updateRequest.getCrownRepOrderDate())
                .isEqualTo(apiProcessRepOrderResponse.getRepOrderDate().toLocalDate());
        softly.assertThat(updateRequest.getCrownWithdrawalDate())
                .isEqualTo(dto.getCrownCourtSummary().getWithdrawalDate());
        softly.assertThat(updateRequest.getEvidenceFeeLevel())
                .isEqualTo(dto.getCrownCourtSummary().getEvidenceFeeLevel());
        softly.assertThat(updateRequest.getIsImprisoned())
                .isEqualTo(dto.getIsImprisoned());
        softly.assertThat(updateRequest.getBankAccountNo())
                .isEqualTo(dto.getPaymentDetails().getBankAccountNo());
        softly.assertThat(updateRequest.getBankAccountName())
                .isEqualTo(dto.getPaymentDetails().getBankAccountName());
        softly.assertThat(updateRequest.getPaymentMethod())
                .isEqualTo(dto.getPaymentDetails().getPaymentMethod());
        softly.assertThat(updateRequest.getPreferredPaymentDay())
                .isEqualTo(dto.getPaymentDetails().getPreferredPaymentDay());
        softly.assertThat(updateRequest.getSortCode())
                .isEqualTo(dto.getPaymentDetails().getSortCode());
        softly.assertThat(updateRequest.getUserModified())
                .isEqualTo(dto.getUserSession().getUserName());
        softly.assertThat(updateRequest.getSentenceOrderDate())
                .isEqualTo(dto.getCrownCourtSummary().getSentenceOrderDate());
        softly.assertAll();
    }
}
