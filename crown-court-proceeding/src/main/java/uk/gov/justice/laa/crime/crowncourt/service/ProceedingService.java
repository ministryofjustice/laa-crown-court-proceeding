package uk.gov.justice.laa.crime.crowncourt.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.builder.UpdateApiResponseBuilder;
import uk.gov.justice.laa.crime.crowncourt.builder.UpdateRepOrderDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.common.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiUpdateApplicationResponse;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.enums.MagCourtOutcome;
import uk.gov.justice.laa.crime.util.SortUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
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


    public ApiUpdateApplicationResponse updateApplication(CrownCourtDTO dto) {
        UpdateRepOrderRequestDTO repOrderRequest = UpdateRepOrderDTOBuilder.build(dto, processRepOrder(dto));
        RepOrderDTO repOrderDTO = maatCourtDataService.updateRepOrder(repOrderRequest);
        ApiUpdateApplicationResponse apiUpdateApplicationResponse = new ApiUpdateApplicationResponse();
        apiUpdateApplicationResponse.withModifiedDateTime(repOrderDTO.getDateModified());
        apiUpdateApplicationResponse.withCrownRepOrderDate(
                ofNullable(repOrderDTO.getCrownRepOrderDate()).map(LocalDate::atStartOfDay).orElse(null));
        apiUpdateApplicationResponse.withCrownRepOrderDecision(repOrderDTO.getCrownRepOrderDecision());
        apiUpdateApplicationResponse.withCrownRepOrderType(repOrderDTO.getCrownRepOrderType());

        return apiUpdateApplicationResponse;
    }

    public ApiUpdateCrownCourtOutcomeResponse update(CrownCourtDTO dto) {
        processRepOrder(dto);
        RepOrderDTO repOrderDTO = repOrderService.updateCCOutcome(dto);
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList = getCCOutcome(dto.getRepId());
        return UpdateApiResponseBuilder.build(repOrderDTO, repOrderCCOutcomeList);
    }

    public List<RepOrderCCOutcomeDTO> getCCOutcome(Integer repId) {
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList =
                maatCourtDataService.getRepOrderCCOutcomeByRepId(repId);
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
