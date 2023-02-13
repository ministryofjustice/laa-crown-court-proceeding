package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedDataServiceTest {

    @InjectMocks
    private ProsecutionConcludedDataService prosecutionConcludedDataService;
    @Mock
    private ProsecutionConcludedRepository prosecutionConcludedRepository;

    @Test
    void givenHearingDataAlreadyExistsWhenExecuteIsCalledThenDataUpdated() {
        when(prosecutionConcludedRepository.getByMaatId(any()))
                .thenReturn(List.of(ProsecutionConcludedEntity.builder().maatId(1234).retryCount(0).build()));
        //given
        prosecutionConcludedDataService.execute(ProsecutionConcluded.
                builder()
                .hearingIdWhereChangeOccurred(UUID.randomUUID())
                .build());
        //then
        verify(prosecutionConcludedRepository, atLeast(1)).saveAll(any());
    }

    @Test
    void givenHearingDataDoesNotExistWhenExecuteIsCalledThenDataCreated() {
        when(prosecutionConcludedRepository.getByMaatId(any()))
                .thenReturn(new ArrayList<>());
        //given
        prosecutionConcludedDataService.execute(ProsecutionConcluded.
                builder()
                .hearingIdWhereChangeOccurred(UUID.randomUUID())
                .build());
        //then
        verify(prosecutionConcludedRepository, atLeast(1)).save(any());
    }

    @Test
    void test_whenUpdateConclusionIsCalledThenDataSaved() {
        //when
        when(prosecutionConcludedRepository.getByMaatId(1234)).thenReturn(List.of(ProsecutionConcludedEntity.builder().build()));
        //given
        prosecutionConcludedDataService.updateConclusion(1234);
        //then
        verify(prosecutionConcludedRepository, atLeast(1)).saveAll(any());
    }
}
