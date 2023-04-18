package uk.gov.justice.laa.crime.crowncourt.builder;

import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.model.*;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;

import java.util.List;

public class UpdateApiResponseBuilder {


    public static ApiUpdateOutcomeResponse build(ApiProcessRepOrderResponse response, RepOrderDTO repOrderDTO,
                                                 List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList) {

        ApiUpdateOutcomeResponse apiUpdateOutcomeResponse = new ApiUpdateOutcomeResponse();
        ApiCrownCourtSummary summary = apiUpdateOutcomeResponse.getCrownCourtSummary();

        summary.withRepType(response.getRepType());
        summary.withRepOrderDate(response.getRepOrderDate());
        summary.withRepOrderDecision(response.getRepOrderDecision());

        if (!repOrderCCOutcomeList.isEmpty()) {
            summary.getCrownCourtOutcome().addAll(repOrderCCOutcomeList.stream().forEach(ccOutcomeDTO -> {
                         new ApiCrownCourtOutcome().withOutcome(CrownCourtOutcome.getFrom(ccOutcomeDTO.getOutcome()))
                        .withDescription(ccOutcomeDTO.getDescription()).withDateSet(ccOutcomeDTO.getOutcomeDate()));
            });
        }

        return apiUpdateOutcomeResponse;
    }
}
