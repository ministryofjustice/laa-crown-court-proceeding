package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCalculateEvidenceFeeRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCapitalEvidence;
import uk.gov.justice.laa.crime.crowncourt.model.ApiEvidenceFee;

public class CrimeEvidenceBuilder {

    public static ApiCalculateEvidenceFeeRequest build(final CrownCourtDTO crownCourtDTO) {

        ApiCalculateEvidenceFeeRequest evidenceFeeRequest = new ApiCalculateEvidenceFeeRequest();
        evidenceFeeRequest.setLaaTransactionId(crownCourtDTO.getLaaTransactionId());
        evidenceFeeRequest.setRepId(crownCourtDTO.getRepId());
        evidenceFeeRequest.setMagCourtOutcome(crownCourtDTO.getMagCourtOutcome().getOutcome());
        if(null != crownCourtDTO.getEvidenceFeeLevel()) {
            ApiEvidenceFee  evidenceFee = new ApiEvidenceFee();
            evidenceFee.setFeeLevel(crownCourtDTO.getEvidenceFeeLevel().getFeeLevel());
            evidenceFee.setDescription(crownCourtDTO.getEvidenceFeeLevel().getDescription());
            evidenceFeeRequest.setEvidenceFee(evidenceFee);
        }
        if (null != crownCourtDTO.getCapitalEvidence()) {
            evidenceFeeRequest.getCapitalEvidence().addAll(crownCourtDTO.getCapitalEvidence().stream().map( evidenceFee ->
                    new ApiCapitalEvidence().withEvidenceType(evidenceFee.getEvidenceType())
                            .withDateReceived(evidenceFee.getDateReceived())).toList());
        }
        evidenceFeeRequest.setIncomeEvidenceReceivedDate(crownCourtDTO.getIncomeEvidenceReceivedDate());
        evidenceFeeRequest.setCapitalEvidenceReceivedDate(crownCourtDTO.getCapitalEvidenceReceivedDate());
        return evidenceFeeRequest;
    }
}
