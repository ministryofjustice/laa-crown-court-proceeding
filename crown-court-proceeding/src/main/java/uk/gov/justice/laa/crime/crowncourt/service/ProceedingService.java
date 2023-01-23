package uk.gov.justice.laa.crime.crowncourt.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.builder.UpdateRepOrderDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MagCourtOutcome;

import java.io.IOException;
import java.util.Set;

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
                (dto.getCaseType() == CaseType.EITHER_WAY &&
                        !dto.getMagCourtOutcome().equals(MagCourtOutcome.RESOLVED_IN_MAGS))) {
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
        processRepOrder(dto);
        maatCourtDataService.updateRepOrder(UpdateRepOrderDTOBuilder.build(dto), dto.getLaaTransactionId());
    }

    public Object graphQLQuery() throws IOException {
        log.info("Start");
        Object obj = maatCourtDataService.getRepOrderByFilter("5639461", "false");
        log.info("Response :" + obj.toString());
        return obj;
    }

}