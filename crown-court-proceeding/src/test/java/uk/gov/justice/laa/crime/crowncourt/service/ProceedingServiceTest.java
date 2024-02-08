package uk.gov.justice.laa.crime.crowncourt.service;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.enums.MagCourtOutcome;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class ProceedingServiceTest {

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
        softly.assertAll();

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

        softly.assertAll();
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

        softly.assertAll();
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

        softly.assertAll();
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

        softly.assertAll();
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

        softly.assertAll();
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

        softly.assertAll();
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

        softly.assertAll();
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
        when(maatCourtDataService.updateRepOrder(any(UpdateRepOrderRequestDTO.class), anyString()))
                .thenReturn(TestModelDataBuilder.getRepOrderDTO());
        proceedingService.updateApplication(requestDTO);
        verify(maatCourtDataService).updateRepOrder(any(UpdateRepOrderRequestDTO.class), anyString());
    }

    @Test
    void givenAValidRepIdAndNoOutcomeRecord_whenGetCCOutcomeIsInvoked_thenReturnEmpty() {
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(Collections.emptyList());
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = proceedingService.getCCOutcome(TestModelDataBuilder.TEST_REP_ID, "1234");
        assertThat(0).isEqualTo(repOrderCCOutcomeDTOList.size());
    }

    @Test
    void givenAInvalidRepId_whenGetCCOutcomeIsInvoked_thenReturnNull() {
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(null);
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = proceedingService.getCCOutcome(TestModelDataBuilder.TEST_REP_ID, "1234");
        assertThat(repOrderCCOutcomeDTOList).isNull();
    }

    @Test
    void givenAValidRepId_whenGetCCOutcomeIsInvoked_thenReturnOutcomeInNaturalOrder() {
        List<RepOrderCCOutcomeDTO> outcomeList = new ArrayList<>();
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(2, CrownCourtOutcome.CONVICTED.getCode(),
                LocalDateTime.of(2023, 2, 7, 15, 1, 25)));
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(3, CrownCourtOutcome.PART_CONVICTED.getCode(),
                LocalDateTime.of(2022, 2, 7, 9, 1, 25)));
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(1, CrownCourtOutcome.SUCCESSFUL.getCode(),
                LocalDateTime.of(2022, 3, 7, 10, 1, 25)));
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(outcomeList);

        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = proceedingService.getCCOutcome(TestModelDataBuilder.TEST_REP_ID, "1234");
        softly.assertThat(repOrderCCOutcomeDTOList.size()).isEqualTo(3);

        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcome()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getDescription()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2022, 2, 7, 9, 1, 25));

        softly.assertThat(repOrderCCOutcomeDTOList.get(1).getOutcome()).isEqualTo(CrownCourtOutcome.SUCCESSFUL.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(1).getDescription()).isEqualTo(CrownCourtOutcome.SUCCESSFUL.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(1).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2022, 3, 7, 10, 1, 25));

        softly.assertThat(repOrderCCOutcomeDTOList.get(2).getOutcome()).isEqualTo(CrownCourtOutcome.CONVICTED.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(2).getDescription()).isEqualTo(CrownCourtOutcome.CONVICTED.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(2).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2023, 2, 7, 15, 1, 25));
        softly.assertAll();

    }

    @Test
    void givenAValidRepIdAndEmptyOutcome_whenGetCCOutcomeIsInvoked_thenReturnOutcome() {
        List<RepOrderCCOutcomeDTO> outcomeList = new ArrayList<>();
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(2, null,
                LocalDateTime.of(2023, 2, 7, 15, 1, 25)));

        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(3, CrownCourtOutcome.PART_CONVICTED.getCode(),
                LocalDateTime.of(2022, 2, 7, 9, 1, 25)));
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(outcomeList);

        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = proceedingService.getCCOutcome(TestModelDataBuilder.TEST_REP_ID, "1234");

        softly.assertThat(repOrderCCOutcomeDTOList.size()).isEqualTo(1);

        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcome()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getDescription()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2022, 2, 7, 9, 1, 25));

        softly.assertAll();
    }

    @Test
    void givenAValidRepIdAndInvalidCrownCourtOutcome_whenGetCCOutcomeIsInvoked_thenReturnOutcomeInNaturalOrder() {
        List<RepOrderCCOutcomeDTO> outcomeList = new ArrayList<>();
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(2, "",
                LocalDateTime.of(2023, 2, 7, 15, 1, 25)));
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(3, CrownCourtOutcome.PART_CONVICTED.getCode(),
                LocalDateTime.of(2022, 2, 7, 9, 1, 25)));
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(1, CrownCourtOutcome.SUCCESSFUL.getCode(),
                LocalDateTime.of(2022, 3, 7, 10, 1, 25)));
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(outcomeList);

        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = proceedingService.getCCOutcome(TestModelDataBuilder.TEST_REP_ID, "1234");
        softly.assertThat(repOrderCCOutcomeDTOList.size()).isEqualTo(2);

        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcome()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getDescription()).isEqualTo(CrownCourtOutcome.PART_CONVICTED.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(0).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2022, 2, 7, 9, 1, 25));

        softly.assertThat(repOrderCCOutcomeDTOList.get(1).getOutcome()).isEqualTo(CrownCourtOutcome.SUCCESSFUL.getCode());
        softly.assertThat(repOrderCCOutcomeDTOList.get(1).getDescription()).isEqualTo(CrownCourtOutcome.SUCCESSFUL.getDescription());
        softly.assertThat(repOrderCCOutcomeDTOList.get(1).getOutcomeDate())
                .isEqualTo(LocalDateTime.of(2022, 3, 7, 10, 1, 25));

        softly.assertAll();

    }

    @Test
    void givenACrownCourtIsEmpty_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtDTO.setCrownCourtSummary(null);
        assertThat(proceedingService.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeIsNull_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtDTO.getCrownCourtSummary().setCrownCourtOutcome(null);
        assertThat(proceedingService.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeIsEmpty_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtDTO.getCrownCourtSummary().setCrownCourtOutcome(new ArrayList<>());
        assertThat(proceedingService.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeIsConvicted_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes = crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.CONVICTED);
        assertThat(proceedingService.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeDateIsNull_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes = crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.CONVICTED);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        assertThat(proceedingService.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtImprisonedIsNullAndConvicted_whenCheckCCDetailsIsInvoked_thenValidationFails() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes = crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.CONVICTED);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        crownCourtDTO.setIsImprisoned(null);
        assertThatThrownBy(() -> {
            proceedingService.checkCCDetails(crownCourtDTO);
        }).isInstanceOf(ValidationException.class).hasMessageContaining("Check Crown Court Details-Imprisoned value must be entered " +
                "for Crown Court Outcome of");
    }

    @ParameterizedTest
    @MethodSource("validateCCOutcomeDetails")
    void givenCCOutcomeIsNotNullAndMagsCourtOutComeIsNull_whenCheckCCDetailsIsInvoked_thenValidationFails(final CrownCourtDTO crownCourtDTO,
                                                                                                          final List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList) {
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(repOrderCCOutcomeDTOList);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> proceedingService.checkCCDetails(crownCourtDTO));
        assertThat(validationException.getMessage()).isEqualTo("Cannot have Crown Court outcome without Mags Court outcome");

    }

    @ParameterizedTest
    @MethodSource("validateCCOutcomeDetailsNoException")
    void givenValidCCDetails_whenCheckCCDetailsIsInvoked_thenValidationPass(final CrownCourtDTO crownCourtDTO,
                                                                                                          final List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList) {
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(repOrderCCOutcomeDTOList);
        assertDoesNotThrow(() -> proceedingService.checkCCDetails(crownCourtDTO));
    }

    private static Stream<Arguments> validateCCOutcomeDetails() {
        return Stream.of(
                Arguments.of(
                        TestModelDataBuilder
                                .getCrownCourtDTO(CaseType.SUMMARY_ONLY, null),
                        TestModelDataBuilder
                                .getRepOrderCCOutcomeDTOList()
                )
        );
    }

    private static Stream<Arguments> validateCCOutcomeDetailsNoException() {
        return Stream.of(
                Arguments.of(
                        TestModelDataBuilder
                                .getCrownCourtDTO(CaseType.APPEAL_CC, null),
                        List.of()
                ),
                Arguments.of(
                        TestModelDataBuilder
                                .getCrownCourtDTO(CaseType.EITHER_WAY, MagCourtOutcome.APPEAL_TO_CC),
                        TestModelDataBuilder
                                .getRepOrderCCOutcomeDTOList()
                ),
                Arguments.of(
                        TestModelDataBuilder
                                .getCrownCourtDTO(null, MagCourtOutcome.APPEAL_TO_CC),
                        List.of()
                ),
                Arguments.of(
                        TestModelDataBuilder
                                .getCrownCourtDTO(CaseType.APPEAL_CC, null),
                        TestModelDataBuilder
                                .getRepOrderCCOutcomeDTOList()
                )
        );
    }

    @Test
    void givenACrownCourtImprisonedIsNullAndPartConvicted_whenCheckCCDetailsIsInvoked_thenValidationFails() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes = crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.PART_CONVICTED);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        crownCourtDTO.setIsImprisoned(null);
        assertThatThrownBy(() -> {
            proceedingService.checkCCDetails(crownCourtDTO);
        }).isInstanceOf(ValidationException.class).hasMessageContaining("Check Crown Court Details-Imprisoned value must be entered " +
                "for Crown Court Outcome of");
    }

    @Test
    void givenACrownCourtImprisonedIsNullAndOutcomeSuccess_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes = crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.SUCCESSFUL);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        crownCourtDTO.getCrownCourtSummary().setIsImprisoned(null);
        assertThat(proceedingService.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenAInput_whenUpdateIsInvoked_thenReturnResponse() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        when(repOrderService.updateCCOutcome(any())).thenReturn(TestModelDataBuilder.getRepOrderDTO());
        List<RepOrderCCOutcomeDTO> outcomeList = new ArrayList<>();
        outcomeList.add(TestModelDataBuilder.getRepOrderCCOutcomeDTO(2, CrownCourtOutcome.CONVICTED.getCode(),
                LocalDateTime.of(2023, 2, 7, 15, 1, 25)));
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(outcomeList);
        ApiUpdateCrownCourtOutcomeResponse response = proceedingService.update(crownCourtDTO);
        ApiCrownCourtSummary summary = response.getCrownCourtSummary();
        assertThat(response.getModifiedDateTime()).isEqualTo(TestModelDataBuilder.TEST_DATE_MODIFIED);
        assertThat(summary.getRepOrderDecision()).isEqualTo("Granted - Passed Means Test");
        assertThat(summary.getRepType()).isEqualTo("Crown Court Only");
        assertThat(summary.getRepOrderDate()).isNotNull();
        assertThat(summary.getRepOrderCrownCourtOutcome().get(0).getOutcome()).isEqualTo(CrownCourtOutcome.CONVICTED);
        assertThat(summary.getRepOrderCrownCourtOutcome().get(0).getOutcomeDate()).isNotNull();
    }
}
