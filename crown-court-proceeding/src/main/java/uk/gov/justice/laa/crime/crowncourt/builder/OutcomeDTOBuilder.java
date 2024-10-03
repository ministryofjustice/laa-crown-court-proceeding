package uk.gov.justice.laa.crime.crowncourt.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.common.model.common.ApiCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class OutcomeDTOBuilder {

    public static List<RepOrderCCOutcomeDTO> build(CrownCourtDTO crownCourtDTO) {
        List<ApiCrownCourtOutcome> outcomes =  crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList = null;
        if (null != outcomes && !outcomes.isEmpty()) {
            log.info("OutcomeDTOBuilder.size()--" + outcomes.size());
            repOrderCCOutcomeList = outcomes.stream().map(outcome ->
                 RepOrderCCOutcomeDTO.builder()
                        .repId(crownCourtDTO.getRepId())
                        .outcome(outcome.getOutcome().getCode())
                        .outcomeDate(null != outcome.getDateSet() ? outcome.getDateSet() : LocalDateTime.now())
                        .userCreated(crownCourtDTO.getUserSession().getUserName())
                        .build()
            ).collect(Collectors.toCollection(ArrayList::new));

        }
        log.info("repOrderCCOutcomeList--" + repOrderCCOutcomeList);
        return repOrderCCOutcomeList;
    }
}
