package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiIOJAppeal;
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

    public static UpdateRepOrderRequestDTO buildOutcome(CrownCourtDTO crownCourtDTO) {
        ApiCrownCourtSummary crownCourtSummary = crownCourtDTO.getCrownCourtSummary();
        ApiIOJAppeal iojAppeal = crownCourtDTO.getIojAppeal();
        return UpdateRepOrderRequestDTO.builder()
                .repId(crownCourtDTO.getRepId())
                .isImprisoned(crownCourtDTO.getIsImprisoned())
                .isWarrantIssued(crownCourtDTO.getIsWarrantIssued())
                .dateModified(LocalDateTime.now())
                .userModified(crownCourtDTO.getUserSession().getUserName())
                .evidenceFeeLevel(crownCourtSummary.getEvidenceFeeLevel())
                .appealTypeCode(iojAppeal.getAppealTypeCode())
                .appealTypeDate(iojAppeal.getAppealTypeDate())
                .build();
    }
}
