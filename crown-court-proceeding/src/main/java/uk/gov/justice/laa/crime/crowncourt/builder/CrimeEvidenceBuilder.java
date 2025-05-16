package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiCapitalEvidence;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiEvidenceFee;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiCalculateEvidenceFeeRequest;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;

@UtilityClass
public class CrimeEvidenceBuilder {

    public static ApiCalculateEvidenceFeeRequest build(CrownCourtDTO crownCourtDTO) {

        ApiCalculateEvidenceFeeRequest evidenceFeeRequest = new ApiCalculateEvidenceFeeRequest();
        evidenceFeeRequest.setRepId(crownCourtDTO.getRepId());
        evidenceFeeRequest.setMagCourtOutcome(crownCourtDTO.getMagCourtOutcome().getOutcome());
        if (null != crownCourtDTO.getEvidenceFeeLevel()) {
            ApiEvidenceFee evidenceFee = new ApiEvidenceFee();
            evidenceFee.setFeeLevel(crownCourtDTO.getEvidenceFeeLevel().getFeeLevel());
            evidenceFee.setDescription(crownCourtDTO.getEvidenceFeeLevel().getDescription());
            evidenceFeeRequest.setEvidenceFee(evidenceFee);
        }
        if (null != crownCourtDTO.getCapitalEvidence()) {
            evidenceFeeRequest
                    .getCapitalEvidence()
                    .addAll(
                            crownCourtDTO.getCapitalEvidence().stream()
                                    .map(
                                            evidenceFee ->
                                                    new ApiCapitalEvidence()
                                                            .withEvidenceType(
                                                                    evidenceFee.getEvidenceType())
                                                            .withDateReceived(
                                                                    evidenceFee.getDateReceived()))
                                    .toList());
        }
        evidenceFeeRequest.setIncomeEvidenceReceivedDate(
                crownCourtDTO.getIncomeEvidenceReceivedDate());
        evidenceFeeRequest.setCapitalEvidenceReceivedDate(
                crownCourtDTO.getCapitalEvidenceReceivedDate());
        evidenceFeeRequest.setEmstCode(crownCourtDTO.getEmstCode());
        return evidenceFeeRequest;
    }
}
