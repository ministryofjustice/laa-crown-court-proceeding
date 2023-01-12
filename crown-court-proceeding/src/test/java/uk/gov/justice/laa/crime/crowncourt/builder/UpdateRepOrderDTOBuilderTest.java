package uk.gov.justice.laa.crime.crowncourt.builder;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;

@ExtendWith(SoftAssertionsExtension.class)
class UpdateRepOrderDTOBuilderTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Test
    void givenCrownCourtDTO_whenBuildIsInvoked_thenCorrectUpdateRepOrderRequestDTOFieldsArePopulated() {
        CrownCourtDTO dto = TestModelDataBuilder.getCrownCourtDTO();
        UpdateRepOrderRequestDTO updateRequest = UpdateRepOrderDTOBuilder.build(dto);

        softly.assertThat(updateRequest.getRepId())
                .isEqualTo(dto.getRepId());
        softly.assertThat(updateRequest.getCrownRepId())
                .isEqualTo(dto.getCrownRepId());
        softly.assertThat(updateRequest.getCrownRepOrderDecision())
                .isEqualTo(dto.getCrownCourtSummary().getRepOrderDecision());
        softly.assertThat(updateRequest.getCrownRepOrderType())
                .isEqualTo(dto.getCrownCourtSummary().getRepType());
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
    }
}
