package uk.gov.justice.laa.crime.crowncourt.service;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MagCourtOutcome;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class CrownCourtProceedingServiceTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @InjectMocks
    private ProceedingService proceedingService;

    @Mock
    private RepOrderService repOrderService;

    @Mock
    private MaatCourtDataService maatCourtDataService;

    private void setupMockData() {
        when(repOrderService.getRepDecision(any(CrownCourtDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
        when(repOrderService.determineCrownRepType(any(CrownCourtDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
        when(repOrderService.determineRepOrderDate(any(CrownCourtDTO.class)))
                .thenReturn(TestModelDataBuilder.getCrownCourtSummary());
    }

    @Test
    void givenValidIndictableCase_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.INDICTABLE);
        setupMockData();
        ApiProcessRepOrderResponse response = proceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidCCAlreadyCase_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.CC_ALREADY);
        setupMockData();
        ApiProcessRepOrderResponse response = proceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidAppealCCCase_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        setupMockData();
        ApiProcessRepOrderResponse response = proceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidCommittalCase_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.COMMITAL);
        setupMockData();
        ApiProcessRepOrderResponse response = proceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithCommittedMagOutcome_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED);
        setupMockData();
        ApiProcessRepOrderResponse response = proceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithCommittedForTrailMagOutcome_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        setupMockData();
        ApiProcessRepOrderResponse response = proceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithSentForTrailMagOutcome_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.SENT_FOR_TRIAL);
        setupMockData();
        ApiProcessRepOrderResponse response = proceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenValidEitherWayCaseWithAppealToCCMagOutcome_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.APPEAL_TO_CC);
        setupMockData();
        ApiProcessRepOrderResponse response = proceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
    }

    @Test
    void givenSummaryOnlyCase_whenProcessRepOrderIsInvoked_emptyResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        assertThat(proceedingService.processRepOrder(requestDTO))
                .isEqualTo(new ApiProcessRepOrderResponse());
    }

    @Test
    void givenEitherWayCaseWithResolvedMagOutcome_whenProcessRepOrderIsInvoked_emptyResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.RESOLVED_IN_MAGS);
        assertThat(proceedingService.processRepOrder(requestDTO))
                .isEqualTo(new ApiProcessRepOrderResponse());
    }

    @Test
    void givenCCApplication_whenUpdateApplicationIsInvoked_thenSentenceOrderDateIsPersisted() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        proceedingService.updateApplication(requestDTO);
        verify(repOrderService).updateCCSentenceOrderDate(any(CrownCourtDTO.class));
        verify(maatCourtDataService).updateRepOrder(any(UpdateRepOrderRequestDTO.class), anyString());
    }


}
