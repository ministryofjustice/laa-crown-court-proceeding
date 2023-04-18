package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.model.ApiRepOrderCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;

import java.util.List;

public class UpdateApiResponseBuilder {


    public static ApiUpdateCrownCourtOutcomeResponse build(ApiProcessRepOrderResponse response, RepOrderDTO repOrderDTO,
                                                           List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList) {

        ApiUpdateCrownCourtOutcomeResponse apiUpdateOutcomeResponse = new ApiUpdateCrownCourtOutcomeResponse();
        ApiCrownCourtSummary summary = apiUpdateOutcomeResponse.getCrownCourtSummary();

        summary.withRepType(response.getRepType());
        summary.withRepOrderDate(response.getRepOrderDate());
        summary.withRepOrderDecision(response.getRepOrderDecision());

        if (!repOrderCCOutcomeList.isEmpty()) {
            repOrderCCOutcomeList.stream().forEach(ccOutcomeDTO -> {

                        summary.getRepOrderCrownCourtOutcome().add(new ApiRepOrderCrownCourtOutcome()
                                .withOutcome(CrownCourtOutcome.getFrom(ccOutcomeDTO.getOutcome()))
                                .withDescription(ccOutcomeDTO.getDescription())
                                .withOutcomeDate(ccOutcomeDTO.getOutcomeDate()));
                    }
            );
        }

        return apiUpdateOutcomeResponse;
    }
}
