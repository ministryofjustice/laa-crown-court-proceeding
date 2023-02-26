package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedValidatorTest {

    @InjectMocks
    private ProsecutionConcludedValidator prosecutionConcludedValidator;

    @Test
    void testWhenProsecutionConcludedRequestIsNull_thenThrowException() {
        Assertions.assertThrows(ValidationException.class, () ->
                prosecutionConcludedValidator.validateRequestObject(null));
    }

    @Test
    void testWhenProsecutionConcludedListIsEmpty_thenThrowException() {
        ProsecutionConcluded request = ProsecutionConcluded.builder().build();
        Assertions.assertThrows(ValidationException.class, () ->
                prosecutionConcludedValidator.validateRequestObject(request));
    }

    @Test
    void testWhenProsecutionConcludedListIsNull_thenThrowException() {
        ProsecutionConcluded request = ProsecutionConcluded.builder().offenceSummary(null)
                .build();
        Assertions.assertThrows(ValidationException.class, () -> {
            prosecutionConcludedValidator.validateRequestObject(request);
        });
        assertThat(request);
    }

    @Test
    void testWhenOuCodeIsNull_thenThrowException() {
        Assertions.assertThrows(ValidationException.class, () -> {
            prosecutionConcludedValidator.validateOuCode(null);
        });
    }

    @Test
    void testWhenOuCodeIsEmpty_thenThrowException() {
        Assertions.assertThrows(ValidationException.class, () -> prosecutionConcludedValidator.validateOuCode(""));
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
        assertThat(prosecutionConcludedValidator.validateOuCode("Test")).isEqualTo(Optional.empty());
    }
}