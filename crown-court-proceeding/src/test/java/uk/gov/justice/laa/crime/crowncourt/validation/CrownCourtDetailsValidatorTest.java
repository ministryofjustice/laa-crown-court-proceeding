package uk.gov.justice.laa.crime.crowncourt.validation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.common.model.common.ApiCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.enums.MagCourtOutcome;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrownCourtDetailsValidatorTest {

    @Mock
    private MaatCourtDataService maatCourtDataService;

    @InjectMocks
    private CrownCourtDetailsValidator crownCourtDetailsValidator;

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
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes =
                crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.CONVICTED);
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeDateIsNull_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes =
                crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.CONVICTED);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @Test
    void givenACrownCourtOutcomeAndImprisonedIsTrue_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes =
                crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        crownCourtDTO.setIsImprisoned(null);
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.CONVICTED);
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("crownCourtOutcomeParameters")
    void givenACrownCourtImprisonedIsNullAndConvicted_whenCheckCCDetailsIsInvoked_thenValidationFails(
            CrownCourtOutcome outcome) {

        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes =
                crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(outcome);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        crownCourtDTO.setIsImprisoned(null);
        assertThatThrownBy(() -> crownCourtDetailsValidator.checkCCDetails(crownCourtDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(
                        "Check Crown Court Details-Imprisoned value must be entered " + "for Crown Court Outcome of");
    }

    @Test
    void givenCCOutcomeIsNotNullAndMagsCourtOutComeIsNull_whenCheckCCDetailsIsInvoked_thenValidationFails() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO(CaseType.SUMMARY_ONLY, null);
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = TestModelDataBuilder.getRepOrderCCOutcomeDTOList();

        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any())).thenReturn(repOrderCCOutcomeDTOList);

        ValidationException validationException =
                assertThrows(ValidationException.class, () -> crownCourtDetailsValidator.checkCCDetails(crownCourtDTO));
        assertThat(validationException.getMessage())
                .isEqualTo(CrownCourtDetailsValidator.CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
    }

    @ParameterizedTest
    @MethodSource("validateCCOutcomeDetailsNoException")
    void givenValidCCDetails_whenCheckCCDetailsIsInvoked_thenValidationPass(
            final CrownCourtDTO crownCourtDTO, final List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList) {

        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(any())).thenReturn(repOrderCCOutcomeDTOList);

        assertThatCode(() -> crownCourtDetailsValidator.checkCCDetails(crownCourtDTO))
                .doesNotThrowAnyException();
    }

    private static Stream<Arguments> crownCourtOutcomeParameters() {
        return Stream.of(Arguments.of(CrownCourtOutcome.CONVICTED), Arguments.of(CrownCourtOutcome.PART_CONVICTED));
    }

    private static Stream<Arguments> validateCCOutcomeDetailsNoException() {
        return Stream.of(
                Arguments.of(TestModelDataBuilder.getCrownCourtDTO(CaseType.APPEAL_CC, null), Collections.emptyList()),
                Arguments.of(
                        TestModelDataBuilder.getCrownCourtDTO(CaseType.EITHER_WAY, MagCourtOutcome.APPEAL_TO_CC),
                        TestModelDataBuilder.getRepOrderCCOutcomeDTOList()),
                Arguments.of(TestModelDataBuilder.getCrownCourtDTO(null, null), null),
                Arguments.of(
                        TestModelDataBuilder.getCrownCourtDTO(CaseType.APPEAL_CC, null),
                        TestModelDataBuilder.getRepOrderCCOutcomeDTOList()));
    }

    @Test
    void givenACrownCourtImprisonedIsNullAndOutcomeSuccess_whenCheckCCDetailsIsInvoked_thenValidationPass() {
        CrownCourtDTO crownCourtDTO = TestModelDataBuilder.getCrownCourtDTO();
        List<ApiCrownCourtOutcome> apiCrownCourtOutcomes =
                crownCourtDTO.getCrownCourtSummary().getCrownCourtOutcome();
        apiCrownCourtOutcomes.get(0).withOutcome(CrownCourtOutcome.SUCCESSFUL);
        apiCrownCourtOutcomes.get(0).setDateSet(null);
        crownCourtDTO.getCrownCourtSummary().setIsImprisoned(null);
        assertThat(crownCourtDetailsValidator.checkCCDetails(crownCourtDTO)).isEmpty();
    }
}
