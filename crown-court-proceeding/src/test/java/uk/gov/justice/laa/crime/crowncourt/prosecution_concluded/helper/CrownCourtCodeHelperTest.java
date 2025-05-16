package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.staticdata.entity.CrownCourtsEntity;
import uk.gov.justice.laa.crime.crowncourt.staticdata.repository.CrownCourtsRepository;
import uk.gov.justice.laa.crime.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
class CrownCourtCodeHelperTest {

    @InjectMocks private CrownCourtCodeHelper crownCourtCodeHelper;

    @Mock private CrownCourtsRepository crownCourtCodeRepository;

    @Test
    void givenUnknownOuCode_whenGetCodeIsInvoked_thenValidationExceptionIsThrown() {
        when(crownCourtCodeRepository.findByOuCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> crownCourtCodeHelper.getCode("8899"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Crown Court Code Look Up Failed for");
    }

    @Test
    void givenOuCodeExists_whenGetCodeIsInvoked_thenCrownCourtCodeIsReturned() {
        Optional<CrownCourtsEntity> optCrownCourtCode =
                Optional.of(CrownCourtsEntity.builder().id("1234").ouCode("8899").build());
        when(crownCourtCodeRepository.findByOuCode("8899")).thenReturn(optCrownCourtCode);

        String code = crownCourtCodeHelper.getCode("8899");

        assertThat(code).isEqualTo("1234");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenOuCode_whenIsValidCodeIsInvoked_thenReturnsCorrectResult(boolean isValid) {
        when(crownCourtCodeRepository.existsByOuCode("8899")).thenReturn(isValid);

        boolean codeExists = crownCourtCodeHelper.isValidCode("8899");

        assertThat(codeExists).isEqualTo(isValid);
    }
}
