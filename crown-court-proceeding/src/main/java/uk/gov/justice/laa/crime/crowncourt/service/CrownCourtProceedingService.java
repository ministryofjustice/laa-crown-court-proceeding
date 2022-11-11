package uk.gov.justice.laa.crime.crowncourt.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrownCourtProceedingService {
    public ApiCheckCrownCourtActionsResponse checkCrownCourtActions(CrownCourtsActionsRequestDTO requestDTO) {
        return new ApiCheckCrownCourtActionsResponse();
    }
}
