package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiCapitalEvidence;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiEvidenceFee;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiCalculateEvidenceFeeRequest;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;

@Slf4j
@UtilityClass
public class CrimeEvidenceBuilder {

    public static ApiCalculateEvidenceFeeRequest build(CrownCourtDTO crownCourtDTO) {
        log.info("crownCourtDTO --+"+ crownCourtDTO);
        ApiCalculateEvidenceFeeRequest evidenceFeeRequest = new ApiCalculateEvidenceFeeRequest();
        evidenceFeeRequest.setRepId(crownCourtDTO.getRepId());
        evidenceFeeRequest.setMagCourtOutcome(crownCourtDTO.getMagCourtOutcome().getOutcome());
        log.info("Mag court outcome --+"+ evidenceFeeRequest.getMagCourtOutcome());
        if (null != crownCourtDTO.getEvidenceFeeLevel()) {
            log.info("has Evidence fee level-- True");
            ApiEvidenceFee evidenceFee = new ApiEvidenceFee();
            evidenceFee.setFeeLevel(crownCourtDTO.getEvidenceFeeLevel().getFeeLevel());
            evidenceFee.setDescription(crownCourtDTO.getEvidenceFeeLevel().getDescription());
            evidenceFeeRequest.setEvidenceFee(evidenceFee);
            log.info("Evidence fees --+"+ evidenceFee);
        }
        if (null != crownCourtDTO.getCapitalEvidence()) {
            log.info("has capitalEvidence -- True");
            evidenceFeeRequest.getCapitalEvidence().addAll(crownCourtDTO.getCapitalEvidence().stream().map(evidenceFee ->
                    new ApiCapitalEvidence().withEvidenceType(evidenceFee.getEvidenceType())
                            .withDateReceived(evidenceFee.getDateReceived())).toList());
            log.info("Evidence fees --+"+ evidenceFeeRequest.getCapitalEvidence());
        }
        evidenceFeeRequest.setIncomeEvidenceReceivedDate(crownCourtDTO.getIncomeEvidenceReceivedDate());
        log.info("IncomeEvidenceReceivedDate --+"+ evidenceFeeRequest.getIncomeEvidenceReceivedDate());
        evidenceFeeRequest.setCapitalEvidenceReceivedDate(crownCourtDTO.getCapitalEvidenceReceivedDate());
        log.info("CapitalEvidenceReceivedDate --+"+ evidenceFeeRequest.getCapitalEvidenceReceivedDate());
        evidenceFeeRequest.setEmstCode(crownCourtDTO.getEmstCode());
        log.info("EmstCode --+"+ evidenceFeeRequest.getCapitalEvidenceReceivedDate());

        log.info("evidenceFeeRequest --+"+ evidenceFeeRequest);
        return evidenceFeeRequest;
    }
}
