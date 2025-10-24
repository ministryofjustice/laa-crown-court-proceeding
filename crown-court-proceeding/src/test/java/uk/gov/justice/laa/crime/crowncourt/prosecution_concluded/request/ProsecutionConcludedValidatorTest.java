package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.request;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.enums.ResultCode;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedValidatorTest {

    @Mock
    private CrownCourtCodeHelper crownCourtCodeHelper;

    @InjectMocks
    private ProsecutionConcludedValidator prosecutionConcludedValidator;

    @Test
    void testWhenProsecutionConcludedRequestIsNull_thenThrowsException() {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void testWhenProsecutionConcludedListIsEmpty_thenThrowsException() {
        ProsecutionConcluded request = ProsecutionConcluded.builder().build();
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void testWhenProsecutionConcludedListIsNull_thenThrowsException() {
        ProsecutionConcluded request =
                ProsecutionConcluded.builder().offenceSummary(null).build();
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void givenAOffenceSummaryIsEmpty_whenValidateRequestObjectIsInvoked_thenThrowsException() {
        ProsecutionConcluded request =
                ProsecutionConcluded.builder().offenceSummary(List.of()).build();
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void givenARepIdIsEmpty_whenValidateRequestObjectIsInvoked_thenThrowsException() {
        ProsecutionConcluded request = TestModelDataBuilder.getProsecutionConcluded();
        request.setMaatId(null);
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void givenAValidRequest_whenValidateRequestObjectIsInvoked_thenDoesNotThrowException() {
        ProsecutionConcluded request = TestModelDataBuilder.getProsecutionConcluded();
        prosecutionConcludedValidator.validateRequestObject(request);
    }

    @Test
    void givenOuCodeIsNull_whenValidateOuCodeIsInvoked_thenThrowsException() {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateOuCode(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.OU_CODE_IS_MISSING);
    }

    @Test
    void givenOuCodeIsEmpty_whenValidateOuCodeIsInvoked_thenThrowsException() {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateOuCode(""))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.OU_CODE_IS_MISSING);
    }

    @Test
    void givenAnUnknownOuCode_whenValidateOuCodeIsInvoked_thenThrowsException() {
        when(crownCourtCodeHelper.isValidCode("Unknown")).thenReturn(false);

        assertThrows(
                ValidationException.class,
                () -> prosecutionConcludedValidator.validateOuCode("Unknown"),
                ProsecutionConcludedValidator.OU_CODE_LOOKUP_FAILED);
    }

    @Test
    void givenAValidOuCode_whenValidateOuCodeIsInvoked_thenNoExceptionIsThrown() {
        when(crownCourtCodeHelper.isValidCode("Test")).thenReturn(true);

        assertDoesNotThrow(() -> prosecutionConcludedValidator.validateOuCode("Test"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"maatId\": \"\"}"})
    void givenMessageContainsNoOrMissingMaatId_whenValidateMaatIdIsInvoked_thenNoExceptionIsThrown(String message) {
        assertDoesNotThrow(() -> prosecutionConcludedValidator.validateMaatId(message));
    }

    @Test
    void givenMessageContainsMaatIdInIncorrectFormat_whenValidateMaatIdIsInvoked_thenThrowsException() {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateMaatId("{\"maatId\": A-1223456}"))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.MAAT_ID_FORMAT_INCORRECT);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void givenMagsCourtOutcomeIsNullOrEmpty_whenValidateMagsCourtOutcomeExistsIsInvoked_thenExceptionIsThrown(
            String magsCourtOutcome) {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateMagsCourtOutcomeExists(magsCourtOutcome))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    void givenResultCodeIsNullOrEmpty_whenValidateApplicationResultCodeIsInvoked_thenExceptionIsThrown(
            String resultCode) {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateApplicationResultCode(resultCode))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.MISSING_APPLICATION_RESULT_CODE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"AAAA", "BBBB"})
    void givenAInvalidResultCode_whenValidateApplicationResultCodeIsInvoked_thenExceptionIsThrown(String resultCode) {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateApplicationResultCode(resultCode))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.INVALID_APPLICATION_RESULT_CODE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"APA", "AACA", "AACD and AASA"})
    void givenAValidResultCode_whenValidateApplicationResultCodeIsInvoked_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> prosecutionConcludedValidator.validateApplicationResultCode(ResultCode.APA.name()));
    }

    @Test
    void givenApplicationConcludeNullOrEmpty_whenAppealMissingIsInvoked_thenExceptionIsThrown() {
        assertThatThrownBy(
                        () -> prosecutionConcludedValidator.validateIsAppealMissing(CaseType.APPEAL_CC.getCaseType()))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.APPEAL_IS_MISSING);
    }

    @Test
    void givenAValidApplicationConclude_whenAppealMissingIsInvoked_thenNoExceptionIsThrown() {
        assertDoesNotThrow(
                () -> prosecutionConcludedValidator.validateIsAppealMissing(CaseType.COMMITAL.getCaseType()));
    }

    @Test
    void givenMagsCourtOutcomeIsNotNullOrEmpty_whenValidateMagsCourtOutcomeExistsIsInvoked_thenNoExceptionIsThrown() {
        assertDoesNotThrow(() -> prosecutionConcludedValidator.validateMagsCourtOutcomeExists("ACQUITTED"));
    }
}
