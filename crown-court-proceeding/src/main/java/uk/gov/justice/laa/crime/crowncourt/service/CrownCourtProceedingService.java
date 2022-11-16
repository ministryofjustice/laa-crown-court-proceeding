package uk.gov.justice.laa.crime.crowncourt.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsResponse;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrownCourtProceedingService {

    private final RepOrderService repOrderService;

    public ApiCheckCrownCourtActionsResponse checkCrownCourtActions(CrownCourtsActionsRequestDTO requestDTO) {
        ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.getRepDecision(requestDTO);
        return new ApiCheckCrownCourtActionsResponse()
                .withRepOrderDecision(apiCrownCourtSummary.getRepOrderDecision())
                .withRepOrderDate(apiCrownCourtSummary.getRepOrderDate());
    }
}
