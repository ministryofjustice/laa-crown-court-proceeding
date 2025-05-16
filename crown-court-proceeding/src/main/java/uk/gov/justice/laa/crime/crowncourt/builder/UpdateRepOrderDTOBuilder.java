package uk.gov.justice.laa.crime.crowncourt.builder;

import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.enums.EvidenceFeeLevel;
import uk.gov.justice.laa.crime.proceeding.MagsDecisionResult;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateRepOrderDTOBuilder {

    public static UpdateRepOrderRequestDTO build(
            CrownCourtDTO crownCourtDTO, ApiProcessRepOrderResponse apiProcessRepOrderResponse) {

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
                                .orElse(null))
                .sentenceOrderDate(crownCourtSummary.getSentenceOrderDate())
                .evidenceFeeLevel(
                        ofNullable(crownCourtSummary.getEvidenceFeeLevel())
                                .map(EvidenceFeeLevel::getFeeLevel)
                                .orElse(null))
                .isImprisoned(crownCourtDTO.getIsImprisoned())
                .userModified(crownCourtDTO.getUserSession().getUserName())
                .build();
    }

    public static UpdateRepOrderRequestDTO build(CrownCourtDTO crownCourtDTO) {
        ApiCrownCourtSummary crownCourtSummary = crownCourtDTO.getCrownCourtSummary();

        return UpdateRepOrderRequestDTO.builder()
                .repId(crownCourtDTO.getRepId())
                .isImprisoned(crownCourtDTO.getIsImprisoned())
                .isWarrantIssued(crownCourtSummary.getIsWarrantIssued())
                .userModified(crownCourtDTO.getUserSession().getUserName())
                .evidenceFeeLevel(
                        ofNullable(crownCourtSummary.getEvidenceFeeLevel())
                                .map(EvidenceFeeLevel::getFeeLevel)
                                .orElse(null))
                .build();
    }

    public static UpdateRepOrderRequestDTO build(
            CrownCourtDTO crownCourtDTO, MagsDecisionResult decisionResult) {
        return UpdateRepOrderRequestDTO.builder()
                .repId(crownCourtDTO.getRepId())
                .decisionDate(decisionResult.getDecisionDate())
                .decisionReasonCode(decisionResult.getDecisionReason())
                .userModified(crownCourtDTO.getUserSession().getUserName())
                .build();
    }
}
