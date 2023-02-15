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
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MagCourtOutcome;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class CrownCourtProceedingServiceTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @InjectMocks
    private CrownCourtProceedingService crownCourtProceedingService;

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
        ApiProcessRepOrderResponse response = crownCourtProceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);
        softly.assertAll();

    }

    @Test
    void givenValidCCAlreadyCase_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.CC_ALREADY);
        setupMockData();
        ApiProcessRepOrderResponse response = crownCourtProceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);

        softly.assertAll();
    }

    @Test
    void givenValidAppealCCCase_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.APPEAL_CC);
        setupMockData();
        ApiProcessRepOrderResponse response = crownCourtProceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);

        softly.assertAll();
    }

    @Test
    void givenValidCommittalCase_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.COMMITAL);
        setupMockData();
        ApiProcessRepOrderResponse response = crownCourtProceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);

        softly.assertAll();
    }

    @Test
    void givenValidEitherWayCaseWithCommittedMagOutcome_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED);
        setupMockData();
        ApiProcessRepOrderResponse response = crownCourtProceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);

        softly.assertAll();
    }

    @Test
    void givenValidEitherWayCaseWithCommittedForTrailMagOutcome_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL);
        setupMockData();
        ApiProcessRepOrderResponse response = crownCourtProceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);

        softly.assertAll();
    }

    @Test
    void givenValidEitherWayCaseWithSentForTrailMagOutcome_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.SENT_FOR_TRIAL);
        setupMockData();
        ApiProcessRepOrderResponse response = crownCourtProceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);

        softly.assertAll();
    }

    @Test
    void givenValidEitherWayCaseWithAppealToCCMagOutcome_whenProcessRepOrderIsInvoked_validResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.APPEAL_TO_CC);
        setupMockData();
        ApiProcessRepOrderResponse response = crownCourtProceedingService.processRepOrder(requestDTO);

        softly.assertThat(response.getRepOrderDecision())
                .isEqualTo(TestModelDataBuilder.MOCK_DECISION);

        softly.assertThat(response.getRepOrderDate())
                .isEqualTo(TestModelDataBuilder.TEST_REP_ORDER_DATE);

        softly.assertAll();
    }

    @Test
    void givenSummaryOnlyCase_whenProcessRepOrderIsInvoked_emptyResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        assertThat(crownCourtProceedingService.processRepOrder(requestDTO))
                .isEqualTo(new ApiProcessRepOrderResponse());
    }

    @Test
    void givenEitherWayCaseWithResolvedMagOutcome_whenProcessRepOrderIsInvoked_emptyResponseIsReturned() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        requestDTO.setCaseType(CaseType.EITHER_WAY);
        requestDTO.setMagCourtOutcome(MagCourtOutcome.RESOLVED_IN_MAGS);
        assertThat(crownCourtProceedingService.processRepOrder(requestDTO))
                .isEqualTo(new ApiProcessRepOrderResponse());
    }

    @Test
    void givenCCApplication_whenUpdateApplicationIsInvoked_thenSentenceOrderDateIsPersisted() {
        CrownCourtDTO requestDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtProceedingService.updateApplication(requestDTO);
        verify(maatCourtDataService).updateRepOrder(any(UpdateRepOrderRequestDTO.class), anyString());
    }

    @Test
    void givenAValidRepIdAndNoOutcomeRecord_whenGetCCOutcome_thenReturnEmpty() {
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(Collections.emptyList());
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = crownCourtProceedingService.getCCOutcome(TestModelDataBuilder.TEST_REP_ID, "1234");
        assertThat(0).isEqualTo(repOrderCCOutcomeDTOList.size());
    }

    @Test
    void givenAInvalidRepId_whenGetCCOutcome_thenReturnNull() {
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(null);
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = crownCourtProceedingService.getCCOutcome(TestModelDataBuilder.TEST_REP_ID, "1234");
        assertThat(repOrderCCOutcomeDTOList).isNull();
    }

    @Test
    void givenAValidRepId_whenGetCCOutcome_thenReturnOutcomeInNaturalOrder() {
        List outcomeList = new ArrayList();
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(2, CrownCourtOutcome.CONVICTED.getCode(),
                LocalDateTime.of(2023, 2, 07, 15, 1, 25)));
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(3, CrownCourtOutcome.PART_CONVICTED.getCode(),
                LocalDateTime.of(2022, 2, 07, 9, 1, 25)));
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(1, CrownCourtOutcome.SUCCESSFUL.getCode(),
                LocalDateTime.of(2022, 3, 07, 10, 1, 25)));
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(outcomeList);

        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = crownCourtProceedingService.getCCOutcome(TestModelDataBuilder.TEST_REP_ID, "1234");
        softly.assertThat(repOrderCCOutcomeDTOList.size()).isEqualTo(3);

        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcome()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getDescription()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2022, 2, 07, 9, 1, 25));

        softly.assertThat(repOrderCCOutcomeDTOList.get(1).getOutcome()).isEqualTo(CrownCourtOutcome.SUCCESSFUL.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(1).getDescription()).isEqualTo(CrownCourtOutcome.SUCCESSFUL.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(1).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2022, 3, 07, 10, 1, 25));

        softly.assertThat(repOrderCCOutcomeDTOList.get(2).getOutcome()).isEqualTo(CrownCourtOutcome.CONVICTED.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(2).getDescription()).isEqualTo(CrownCourtOutcome.CONVICTED.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(2).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2023, 2, 07, 15, 1, 25));
        softly.assertAll();

    }

    @Test
    void givenAValidRepIdAndEmptyOutcome_whenGetCCOutcome_thenReturnOutcome() {
        List outcomeList = new ArrayList();
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(2, null,
                LocalDateTime.of(2023, 2, 07, 15, 1, 25)));

        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(3, CrownCourtOutcome.PART_CONVICTED.getCode(),
                LocalDateTime.of(2022, 2, 07, 9, 1, 25)));
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(outcomeList);

        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = crownCourtProceedingService.getCCOutcome(TestModelDataBuilder.TEST_REP_ID, "1234");

        softly.assertThat(repOrderCCOutcomeDTOList.size()).isEqualTo(1);

        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcome()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getDescription()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2022, 2, 07, 9, 1, 25));

        softly.assertAll();

    }


}
