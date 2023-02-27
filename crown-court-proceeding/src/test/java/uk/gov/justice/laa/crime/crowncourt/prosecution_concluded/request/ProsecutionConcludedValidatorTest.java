package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedValidatorTest {

    @InjectMocks
    private ProsecutionConcludedValidator prosecutionConcludedValidator;

    @Test
    void testWhenProsecutionConcludedRequestIsNull_thenThrowException() {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void testWhenProsecutionConcludedListIsEmpty_thenThrowException() {
        ProsecutionConcluded request = ProsecutionConcluded.builder().build();
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void testWhenProsecutionConcludedListIsNull_thenThrowException() {
        ProsecutionConcluded request = ProsecutionConcluded.builder().offenceSummary(null).build();
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(request))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void testWhenOuCodeIsNull_thenThrowException() {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateOuCode(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.OU_CODE_IS_MISSING);
    }

    @Test
    void testWhenOuCodeIsEmpty_thenThrowException() {
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateOuCode(""))
                .isInstanceOf(ValidationException.class)
                .hasMessage(ProsecutionConcludedValidator.OU_CODE_IS_MISSING);
    }

    @Test
    void givenAOffenceSummaryIsEmpty_whenValidateRequestObjectIsInvoked_thenThrowException() {
        ProsecutionConcluded request = ProsecutionConcluded.builder().offenceSummary(List.of())
                .build();
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(request))
                .isInstanceOf(ValidationException.class).hasMessageContaining("Payload is not available or null");

    }

    @Test
    void givenARepIdIsEmpty_whenValidateRequestObjectIsInvoked_thenThrowException() {
        ProsecutionConcluded request = TestModelDataBuilder.getProsecutionConcluded();
        request.setMaatId(null);
        assertThatThrownBy(() -> prosecutionConcludedValidator.validateRequestObject(request))
                .isInstanceOf(ValidationException.class).hasMessageContaining("Payload is not available or null");
    }

    @Test
    void givenAValidRequest_whenValidateRequestObjectIsInvoked_thenNotThrowException() {
        ProsecutionConcluded request = TestModelDataBuilder.getProsecutionConcluded();
        prosecutionConcludedValidator.validateRequestObject(request);
    }

    @Test
    void givenAValidOuCode_whenValidateOuCodeIsInvoked_thenReturnEmpty() {
        assertThat(prosecutionConcludedValidator.validateOuCode("Test")).isEmpty();
    }
}