package uk.gov.justice.laa.crime.crowncourt.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrownCourtProceedingServiceTest {

    @InjectMocks
    private CrownCourtProceedingService crownCourtProceedingService;
    @Mock
    private RepOrderService repOrderService;

    @Test
    void givenValidCCActionsRequest_whenCheckCrownCourtActionsIsInvoked_validResponseIsReturned() {
        CrownCourtsActionsRequestDTO requestDTO = TestModelDataBuilder.getCrownCourtActionsRequestDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        when(repOrderService.getRepDecision(any(CrownCourtsActionsRequestDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
        ApiCheckCrownCourtActionsResponse response = crownCourtProceedingService.checkCrownCourtActions(requestDTO);
        assertThat(response.getRepOrderDecision()).isEqualTo(TestModelDataBuilder.MOCK_DECISION);
        assertThat(response.getRepOrderDate()).isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }
}
