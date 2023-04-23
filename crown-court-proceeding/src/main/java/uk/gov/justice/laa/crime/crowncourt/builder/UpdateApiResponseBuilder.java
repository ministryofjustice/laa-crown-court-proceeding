package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiRepOrderCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;

import java.util.List;

import static java.util.Optional.ofNullable;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateApiResponseBuilder {


    public static ApiUpdateCrownCourtOutcomeResponse build(RepOrderDTO repOrderDTO, List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList) {

        ApiUpdateCrownCourtOutcomeResponse apiUpdateOutcomeResponse = new ApiUpdateCrownCourtOutcomeResponse();
        ApiCrownCourtSummary summary = new ApiCrownCourtSummary();

        if (null != repOrderDTO) {
            apiUpdateOutcomeResponse.setModifiedDateTime(repOrderDTO.getDateModified());
            summary.withRepType(repOrderDTO.getCrownRepOrderType());
            summary.withRepOrderDate(ofNullable(repOrderDTO.getCrownRepOrderDate().atStartOfDay()).orElse(null));
            summary.withRepOrderDecision(repOrderDTO.getCrownRepOrderDecision());
            summary.setEvidenceFeeLevel(repOrderDTO.getEvidenceFeeLevel());

            if (!repOrderCCOutcomeList.isEmpty()) {
                repOrderCCOutcomeList.stream().forEach(ccOutcomeDTO ->
                        summary.getRepOrderCrownCourtOutcome().add(new ApiRepOrderCrownCourtOutcome()
                                .withOutcome(CrownCourtOutcome.getFrom(ccOutcomeDTO.getOutcome()))
                                .withOutcomeDate(ccOutcomeDTO.getOutcomeDate()))
                );
            }
            apiUpdateOutcomeResponse.setCrownCourtSummary(summary);
        }

        return apiUpdateOutcomeResponse;
    }
}
