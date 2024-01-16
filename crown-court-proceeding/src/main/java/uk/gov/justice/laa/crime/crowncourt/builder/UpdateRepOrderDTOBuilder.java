package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;

import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateRepOrderDTOBuilder {

    public static UpdateRepOrderRequestDTO build(CrownCourtDTO crownCourtDTO, ApiProcessRepOrderResponse apiProcessRepOrderResponse) {
        ApiCrownCourtSummary crownCourtSummary = crownCourtDTO.getCrownCourtSummary();
        return UpdateRepOrderRequestDTO.builder()
                .repId(crownCourtDTO.getRepId())
                .crownRepId(crownCourtDTO.getCrownRepId())
                .crownRepOrderDecision(apiProcessRepOrderResponse.getRepOrderDecision())
                .crownRepOrderType(apiProcessRepOrderResponse.getRepType())
                .crownRepOrderDate(
                        ofNullable(apiProcessRepOrderResponse.getRepOrderDate())
                        .map(LocalDateTime::toLocalDate)
                        .orElse(null))
                .crownWithdrawalDate(
                        ofNullable(crownCourtSummary.getWithdrawalDate())
                                .map(LocalDateTime::toLocalDate)
                                .orElse(null)
                )
                .sentenceOrderDate(crownCourtSummary.getSentenceOrderDate())
                .evidenceFeeLevel(crownCourtSummary.getEvidenceFeeLevel())
                .isImprisoned(crownCourtDTO.getIsImprisoned())
                .userModified(crownCourtDTO.getUserSession().getUserName())
                .build();
    }

    public static UpdateRepOrderRequestDTO buildOutcome(CrownCourtDTO crownCourtDTO) {
        ApiCrownCourtSummary crownCourtSummary = crownCourtDTO.getCrownCourtSummary();
        return UpdateRepOrderRequestDTO.builder()
                .repId(crownCourtDTO.getRepId())
                .isImprisoned(crownCourtDTO.getIsImprisoned())
                .isWarrantIssued(crownCourtSummary.getIsWarrantIssued())
                .userModified(crownCourtDTO.getUserSession().getUserName())
                .evidenceFeeLevel(crownCourtSummary.getEvidenceFeeLevel())
                .build();
    }
}
