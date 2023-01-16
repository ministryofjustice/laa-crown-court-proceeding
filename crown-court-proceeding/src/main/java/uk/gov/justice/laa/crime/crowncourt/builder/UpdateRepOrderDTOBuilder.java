package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiPaymentDetails;

import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateRepOrderDTOBuilder {

    public static UpdateRepOrderRequestDTO build(CrownCourtDTO crownCourtDTO) {
        ApiPaymentDetails paymentDetails = crownCourtDTO.getPaymentDetails();
        ApiCrownCourtSummary crownCourtSummary = crownCourtDTO.getCrownCourtSummary();
        return UpdateRepOrderRequestDTO.builder()
                .repId(crownCourtDTO.getRepId())
                .crownRepId(crownCourtDTO.getCrownRepId())
                .crownRepOrderDecision(crownCourtSummary.getRepOrderDecision())
                .crownRepOrderType(crownCourtSummary.getRepType())
                .crownWithdrawalDate(
                        ofNullable(crownCourtSummary.getWithdrawalDate())
                                .map(LocalDateTime::toLocalDate)
                                .orElse(null)
                )
                .sentenceOrderDate(crownCourtSummary.getSentenceOrderDate())
                .evidenceFeeLevel(crownCourtSummary.getEvidenceFeeLevel())
                .isImprisoned(crownCourtDTO.getIsImprisoned())
                .bankAccountName(paymentDetails.getBankAccountName())
                .bankAccountNo(paymentDetails.getBankAccountNo())
                .paymentMethod(paymentDetails.getPaymentMethod())
                .preferredPaymentDay(paymentDetails.getPreferredPaymentDay())
                .sortCode(paymentDetails.getSortCode())
                .userModified(crownCourtDTO.getUserSession().getUserName())
                .build();
    }
}
