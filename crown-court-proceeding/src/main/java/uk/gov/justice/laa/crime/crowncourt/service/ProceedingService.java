package uk.gov.justice.laa.crime.crowncourt.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.builder.UpdateRepOrderDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MagCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.util.SortUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProceedingService {

    private final RepOrderService repOrderService;
    private final MaatCourtDataService maatCourtDataService;

    private final Set<CaseType> caseTypes = Set.of(
            CaseType.INDICTABLE, CaseType.CC_ALREADY, CaseType.APPEAL_CC, CaseType.COMMITAL
    );

    public ApiProcessRepOrderResponse processRepOrder(CrownCourtDTO dto) {
        ApiProcessRepOrderResponse apiProcessRepOrderResponse = new ApiProcessRepOrderResponse();
        if (caseTypes.contains(dto.getCaseType()) ||
                (CaseType.EITHER_WAY == dto.getCaseType() &&
                        MagCourtOutcome.RESOLVED_IN_MAGS != dto.getMagCourtOutcome())) {
            repOrderService.getRepDecision(dto);
            repOrderService.determineCrownRepType(dto);
            ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.determineRepOrderDate(dto);
            apiProcessRepOrderResponse
                    .withRepOrderDecision(apiCrownCourtSummary.getRepOrderDecision())
                    .withRepOrderDate(apiCrownCourtSummary.getRepOrderDate())
                    .withRepType(apiCrownCourtSummary.getRepType());
        }
        return apiProcessRepOrderResponse;
    }

    public void updateApplication(CrownCourtDTO dto) {
        ApiProcessRepOrderResponse apiProcessRepOrderResponse = processRepOrder(dto);
        maatCourtDataService.updateRepOrder(UpdateRepOrderDTOBuilder.build(dto, apiProcessRepOrderResponse), dto.getLaaTransactionId());
    }

    public Object graphQLQuery() throws IOException {
        log.info("Start");
        Object obj = maatCourtDataService.getRepOrderByFilter("5639461", "false");
        log.info("Response :" + obj.toString());
        return obj;
    }

    public List<RepOrderCCOutcomeDTO> getCCOutcome(Integer repId, String laaTransactionId) {
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList = maatCourtDataService.getRepOrderCCOutcomeByRepId(repId, laaTransactionId);
        if (null != repOrderCCOutcomeList && !repOrderCCOutcomeList.isEmpty()) {
            repOrderCCOutcomeList = repOrderCCOutcomeList.stream().filter(outcome ->
                    isNotBlank(outcome.getOutcome())).collect(Collectors.toCollection(ArrayList::new));
            SortUtils.sortListWithComparing(repOrderCCOutcomeList, RepOrderCCOutcomeDTO::getOutcomeDate,
                    RepOrderCCOutcomeDTO::getId, SortUtils.getComparator());
            repOrderCCOutcomeList.forEach(outcome -> {
                CrownCourtOutcome crownCourtOutcome = CrownCourtOutcome.getFrom(outcome.getOutcome());
                if (crownCourtOutcome != null) {
                    outcome.setDescription(crownCourtOutcome.getDescription());
                }
            });
        }
        return repOrderCCOutcomeList;
    }
}
