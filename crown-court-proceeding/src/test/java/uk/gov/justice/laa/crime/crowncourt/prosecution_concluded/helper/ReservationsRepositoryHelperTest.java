package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.entity.ReservationsEntity;
import uk.gov.justice.laa.crime.crowncourt.repository.ReservationsRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationsRepositoryHelperTest {

    @InjectMocks
    private ReservationsRepositoryHelper reservationsRepositoryHelper;

    @Mock
    private ReservationsRepository reservationsRepository;

    @Test
    void testWhenMaatIsNotLocked_thenReturnTrue() {
        Optional<ReservationsEntity> reservationsEntity = Optional
                .of(ReservationsEntity.builder()
                        .build());
        when(reservationsRepository.findById(anyInt())).thenReturn(reservationsEntity);

        boolean status = reservationsRepositoryHelper.isMaatRecordLocked(anyInt());

        verify(reservationsRepository).findById(anyInt());

        assertThat(status).isTrue();
    }

    @Test
    void testWhenMaatIsLocked_thenReturnFalse() {
        Optional<ReservationsEntity> reservationsEntity = Optional.empty();
        when(reservationsRepository.findById(anyInt())).thenReturn(reservationsEntity);

        boolean status = reservationsRepositoryHelper.isMaatRecordLocked(anyInt());

        verify(reservationsRepository).findById(anyInt());
        assertThat(status).isFalse();
    }
}