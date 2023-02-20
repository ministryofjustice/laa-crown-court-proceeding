package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.exception.MAATCourtDataException;
import uk.gov.justice.laa.crime.crowncourt.staticdata.entity.CrownCourtsEntity;
import uk.gov.justice.laa.crime.crowncourt.staticdata.repository.CrownCourtsRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrownCourtCodeHelperTest {

    @InjectMocks
    private CrownCourtCodeHelper crownCourtCodeHelper;

    @Mock
    private CrownCourtsRepository crownCourtCodeRepository;

    @Test
    void testWhenOuCodeFound_thenReturnCode() {

        Optional<CrownCourtsEntity> optCrownCourtCode = Optional.of(
                CrownCourtsEntity.builder()
                        .id("1234")
                        .ouCode("8899")
                        .build());
        when(crownCourtCodeRepository.findByOuCode(anyString())).thenReturn(optCrownCourtCode);

        String code = crownCourtCodeHelper.getCode(anyString());

        verify(crownCourtCodeRepository).findByOuCode(anyString());
        assertThat(code).isEqualTo("1234");
    }

    @Test
    void testWhenOuCodeNotFound_thenThrowMAATCourtDataException() {
        when(crownCourtCodeRepository.findByOuCode(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> crownCourtCodeHelper.getCode("")).isInstanceOf(MAATCourtDataException.class)
                .hasMessageContaining("Crown Court Code Look Up Failed for");
    }
}