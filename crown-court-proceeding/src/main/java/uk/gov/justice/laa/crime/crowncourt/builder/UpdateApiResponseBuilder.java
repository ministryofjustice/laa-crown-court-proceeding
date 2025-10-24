package uk.gov.justice.laa.crime.crowncourt.builder;

import static java.util.Optional.ofNullable;

import lombok.experimental.UtilityClass;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiRepOrderCrownCourtOutcome;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.enums.CrownCourtOutcome;

import java.util.List;

@UtilityClass
public class UpdateApiResponseBuilder {

    public static ApiUpdateCrownCourtOutcomeResponse build(
            RepOrderDTO repOrderDTO, List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList) {

        ApiUpdateCrownCourtOutcomeResponse apiUpdateOutcomeResponse = new ApiUpdateCrownCourtOutcomeResponse();
        ApiCrownCourtSummary summary = new ApiCrownCourtSummary();

        apiUpdateOutcomeResponse.setModifiedDateTime(repOrderDTO.getDateModified());
        summary.withRepType(repOrderDTO.getCrownRepOrderType());
        summary.withRepOrderDate(ofNullable(repOrderDTO.getCrownRepOrderDate())
                .map(o -> o.atStartOfDay())
                .orElse(null));
        summary.withRepOrderDecision(repOrderDTO.getCrownRepOrderDecision());
        summary.setEvidenceFeeLevel(repOrderDTO.getEvidenceFeeLevel());
        apiUpdateOutcomeResponse.setCrownCourtSummary(summary);

        if (!repOrderCCOutcomeList.isEmpty()) {
            repOrderCCOutcomeList.stream().forEach(ccOutcomeDTO -> summary.getRepOrderCrownCourtOutcome()
                    .add(new ApiRepOrderCrownCourtOutcome()
                            .withOutcome(CrownCourtOutcome.getFrom(ccOutcomeDTO.getOutcome()))
                            .withOutcomeDate(ccOutcomeDTO.getOutcomeDate())));
            apiUpdateOutcomeResponse.setCrownCourtSummary(summary);
        }
        return apiUpdateOutcomeResponse;
    }
}
