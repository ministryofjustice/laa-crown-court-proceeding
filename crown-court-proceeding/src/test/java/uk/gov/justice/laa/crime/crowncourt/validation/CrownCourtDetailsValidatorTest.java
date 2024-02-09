package uk.gov.justice.laa.crime.crowncourt.validation;

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
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.enums.MagCourtOutcome;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrownCourtDetailsValidatorTest {

    @InjectMocks
    private CrownCourtDetailsValidator crownCourtDetailsValidator;

    @Mock
    private MaatCourtDataService maatCourtDataService;

    @Test
    void givenACrownCourtIsEmpty_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtDTO.setCrownCourtSummary(null);
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeIsNull_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtDTO.getCrownCourtSummary().setCrownCourtOutcome(null);
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeIsEmpty_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        crownCourtDTO.getCrownCourtSummary().setCrownCourtOutcome(new ArrayList<>());
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeIsConvicted_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes = crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.CONVICTED);
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeDateIsNull_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes = crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.CONVICTED);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtImprisonedIsNullAndConvicted_whenCheckCCDetailsIsInvoked_thenValidationFails() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes = crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.CONVICTED);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        crownCourtDTO.setIsImprisoned(null);
        assertThatThrownBy(() -> crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isInstanceOf(ValidationException.class).hasMessageContaining("Check Crown Court Details-Imprisoned value must be entered " +
                "for Crown Court Outcome of");
    }

    @ParameterizedTest
    @MethodSource("validateCCOutcomeDetails")
    void givenCCOutcomeIsNotNullAndMagsCourtOutComeIsNull_whenCheckCCDetailsIsInvoked_thenValidationFails(
            final CrownCourtDTO crownCourtDTO,
            final List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList) {
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(repOrderCCOutcomeDTOList);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> crownCourtDetailsValidator.checkCCDetails(crownCourtDTO));
        assertThat(validationException.getMessage()).isEqualTo("Cannot have Crown Court outcome without Mags Court outcome");

    }

    @ParameterizedTest
    @MethodSource("validateCCOutcomeDetailsNoException")
    void givenValidCCDetails_whenCheckCCDetailsIsInvoked_thenValidationPass(
            final CrownCourtDTO crownCourtDTO,
            final List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList) {
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any(), any())).thenReturn(repOrderCCOutcomeDTOList);
        assertDoesNotThrow(() -> crownCourtDetailsValidator.checkCCDetails(crownCourtDTO));
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
        assertThatThrownBy(() -> crownCourtDetailsValidator.checkCCDetails(crownCourtDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Check Crown Court Details-Imprisoned value must be entered for Crown Court Outcome of");
    }

    @Test
    void givenACrownCourtImprisonedIsNullAndOutcomeSuccess_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes = crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.SUCCESSFUL);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        crownCourtDTO.getCrownCourtSummary().setIsImprisoned(null);
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }
}
