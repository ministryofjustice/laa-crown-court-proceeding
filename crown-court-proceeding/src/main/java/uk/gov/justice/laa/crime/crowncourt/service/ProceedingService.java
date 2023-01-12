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
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProceedingService {

    private final RepOrderService repOrderService;
    private final MaatCourtDataService maatCourtDataService;

    private final List<CaseType> caseTypes = List.of(
            CaseType.INDICTABLE,
            CaseType.CC_ALREADY,
            CaseType.APPEAL_CC,
            CaseType.COMMITAL
    );
    private final List<MagCourtOutcome> magCourtOutcomes = List.of(
            MagCourtOutcome.COMMITTED_FOR_TRIAL,
            MagCourtOutcome.SENT_FOR_TRIAL,
            MagCourtOutcome.COMMITTED,
            MagCourtOutcome.APPEAL_TO_CC
    );

    public ApiProcessRepOrderResponse processRepOrder(CrownCourtDTO dto) {
        ApiProcessRepOrderResponse apiProcessCrownRepOrderResponse = new ApiProcessRepOrderResponse();
        if (caseTypes.contains(dto.getCaseType()) ||
                (dto.getCaseType() == CaseType.EITHER_WAY && magCourtOutcomes.contains(dto.getMagCourtOutcome()))) {
            repOrderService.getRepDecision(dto);
            repOrderService.determineCrownRepType(dto);
            ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.determineRepOrderDate(dto);
            apiProcessCrownRepOrderResponse
                    .withRepOrderDecision(apiCrownCourtSummary.getRepOrderDecision())
                    .withRepOrderDate(apiCrownCourtSummary.getRepOrderDate())
                    .withRepType(apiCrownCourtSummary.getRepType());
        }
        return apiProcessCrownRepOrderResponse;
    }

    public void updateApplication(CrownCourtDTO dto) {
        processRepOrder(dto);
        repOrderService.updateCCSentenceOrderDate(dto);
        maatCourtDataService.updateRepOrder(UpdateRepOrderDTOBuilder.build(dto), dto.getLaaTransactionId());
    }

    public Object graphQLQuery() throws URISyntaxException, IOException {
        log.info("Start");
        Object obj = maatCourtDataService.getRepOrderByFilter("5639461", "false");
        log.info("Response :" + obj.toString() );
        return obj;
    }

}
