package uk.gov.justice.laa.crime.crowncourt.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.ProcessCrownRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtApplicationRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessCrownRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MagCourtOutcome;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrownCourtProceedingService {

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

    public ApiProcessCrownRepOrderResponse processCrownRepOrder(ProcessCrownRepOrderRequestDTO requestDTO) {
        ApiProcessCrownRepOrderResponse apiProcessCrownRepOrderResponse = new ApiProcessCrownRepOrderResponse();
        if (caseTypes.contains(requestDTO.getCaseType()) ||
                (requestDTO.getCaseType() == CaseType.EITHER_WAY && magCourtOutcomes.contains(requestDTO.getMagCourtOutcome()))) {
            repOrderService.getRepDecision(requestDTO);
            repOrderService.determineCrownRepType(requestDTO);
            ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.determineRepOrderDate(requestDTO);
            apiProcessCrownRepOrderResponse
                    .withRepOrderDecision(apiCrownCourtSummary.getRepOrderDecision())
                    .withRepOrderDate(apiCrownCourtSummary.getRepOrderDate())
                    .withRepType(apiCrownCourtSummary.getRepType());
        }
        return apiProcessCrownRepOrderResponse;
    }

    public void updateCrownCourtApplication(CrownCourtApplicationRequestDTO crownCourtApplicationRequestDTO) {
        repOrderService.updateCCSentenceOrderDate(crownCourtApplicationRequestDTO);
    }

    public Object graphQLQuery() throws URISyntaxException, IOException {
        log.info("Start");
        Object obj = maatCourtDataService.getRepOrderByFilter("5639461", "false");
        log.info("Response :" + obj.toString() );
        return obj;
    }

}
