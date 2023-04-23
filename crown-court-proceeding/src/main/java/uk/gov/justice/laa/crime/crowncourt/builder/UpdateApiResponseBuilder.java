package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiRepOrderCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;

import java.util.List;

import static java.util.Optional.ofNullable;

public class UpdateApiResponseBuilder {


    public static ApiUpdateCrownCourtOutcomeResponse build(RepOrderDTO repOrderDTO, List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList) {

        ApiUpdateCrownCourtOutcomeResponse apiUpdateOutcomeResponse = new ApiUpdateCrownCourtOutcomeResponse();
        ApiCrownCourtSummary summary = new ApiCrownCourtSummary();

        apiUpdateOutcomeResponse.setModifiedDateTime(repOrderDTO.getDateModified());

        if (null != repOrderDTO) {
            summary.withRepType(repOrderDTO.getCrownRepOrderType());
            summary.withRepOrderDate(ofNullable(repOrderDTO.getCrownRepOrderDate().atStartOfDay()).orElse(null));
            summary.withRepOrderDecision(repOrderDTO.getCrownRepOrderDecision());
            summary.setEvidenceFeeLevel(repOrderDTO.getEvidenceFeeLevel());

            if (!repOrderCCOutcomeList.isEmpty()) {
                repOrderCCOutcomeList.stream().forEach(ccOutcomeDTO -> {

                            summary.getRepOrderCrownCourtOutcome().add(new ApiRepOrderCrownCourtOutcome()
                                    .withOutcome(CrownCourtOutcome.getFrom(ccOutcomeDTO.getOutcome()))
                                    .withOutcomeDate(ccOutcomeDTO.getOutcomeDate()));
                        }
                );
            }
            apiUpdateOutcomeResponse.setCrownCourtSummary(summary);
        }

        return apiUpdateOutcomeResponse;
    }
}
