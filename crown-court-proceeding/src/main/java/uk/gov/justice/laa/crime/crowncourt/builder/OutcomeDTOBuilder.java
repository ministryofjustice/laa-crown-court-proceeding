package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.crime.common.model.common.ApiCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OutcomeDTOBuilder {

    public static List<RepOrderCCOutcomeDTO> build(CrownCourtDTO crownCourtDTO) {
        List<ApiCrownCourtOutcome> outcomes =
                crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList = null;
        if (null != outcomes && !outcomes.isEmpty()) {
            repOrderCCOutcomeList = outcomes.stream()
                    .map(outcome -> RepOrderCCOutcomeDTO.builder()
                            .repId(crownCourtDTO.getRepId())
                            .outcome(outcome.getOutcome().getCode())
                            .outcomeDate(null != outcome.getDateSet() ? outcome.getDateSet() : LocalDateTime.now())
                            .userCreated(crownCourtDTO.getUserSession().getUserName())
                            .build())
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return repOrderCCOutcomeList;
    }
}
