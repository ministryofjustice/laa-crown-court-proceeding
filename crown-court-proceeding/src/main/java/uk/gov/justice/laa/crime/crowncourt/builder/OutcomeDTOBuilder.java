package uk.gov.justice.laa.crime.crowncourt.builder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.common.model.common.ApiCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OutcomeDTOBuilder {

    public static List<RepOrderCCOutcomeDTO> build(CrownCourtDTO crownCourtDTO) {
        List<ApiCrownCourtOutcome> outcomes =
                crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();

        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList = null;

        if (null != outcomes && !outcomes.isEmpty()) {
            repOrderCCOutcomeList = mapRepOrderOutcomes(outcomes, crownCourtDTO);
        }

        return repOrderCCOutcomeList;
    }

    private static List<RepOrderCCOutcomeDTO> mapRepOrderOutcomes(
            List<ApiCrownCourtOutcome> outcomes, CrownCourtDTO crownCourtDTO) {
        return outcomes.stream()
                .map(
                        outcome ->
                                RepOrderCCOutcomeDTO.builder()
                                        .repId(crownCourtDTO.getRepId())
                                        .outcome(outcome.getOutcome().getCode())
                                        .outcomeDate(
                                                null != outcome.getDateSet()
                                                        ? outcome.getDateSet()
                                                        : LocalDateTime.now())
                                        .userCreated(crownCourtDTO.getUserSession().getUserName())
                                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
