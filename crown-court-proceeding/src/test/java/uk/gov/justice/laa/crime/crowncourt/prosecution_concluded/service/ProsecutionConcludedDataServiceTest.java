package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedDataServiceTest {

    @InjectMocks
    private ProsecutionConcludedDataService prosecutionConcludedDataService;
    @Mock
    private ProsecutionConcludedRepository prosecutionConcludedRepository;
    @Mock
    private ObjectMapper objectMapper;

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
        verify(prosecutionConcludedRepository).saveAll(any());
    }

    @Test
    void givenHearingDataDoesNotExistWhenExecuteIsCalledThenDataCreated() throws JsonProcessingException {
        when(prosecutionConcludedRepository.getByMaatId(any()))
                .thenReturn(new ArrayList<>());
        //given
        prosecutionConcludedDataService.execute(ProsecutionConcluded.
                builder()
                .hearingIdWhereChangeOccurred(UUID.randomUUID())
                .build());
        //then
        verify(prosecutionConcludedRepository).save(any());
        verify(objectMapper).writeValueAsBytes(any());
    }

    @Test
    void test_whenUpdateConclusionIsCalledThenDataSaved() {
        //when
        when(prosecutionConcludedRepository.getByMaatId(1234)).thenReturn(List.of(ProsecutionConcludedEntity.builder().build()));
        //given
        prosecutionConcludedDataService.updateConclusion(1234);
        //then
        verify(prosecutionConcludedRepository).saveAll(any());
    }

    @Test
    void givenAInvalidCaseData_whenExecuteIsInvoked_thenSaveIsFailed() throws JsonProcessingException {
        when(prosecutionConcludedRepository.getByMaatId(any()))
                .thenReturn(new ArrayList<>());
        when(objectMapper.writeValueAsBytes(any())).thenThrow(JsonProcessingException.class);
        //given
        prosecutionConcludedDataService.execute(ProsecutionConcluded.
                builder()
                .hearingIdWhereChangeOccurred(UUID.randomUUID())
                .build());
        //then
        verify(prosecutionConcludedRepository, times(0)).save(any());
    }


    @Test
    void givenAValidParameter_whenGetCountByMaatIdAndStatusIsInvoked_thenCountIsReturned() {
        prosecutionConcludedDataService.getCountByMaatIdAndStatus(TestModelDataBuilder.TEST_REP_ID, CaseConclusionStatus.PENDING.name());
        verify(prosecutionConcludedRepository).countByMaatIdAndStatus(TestModelDataBuilder.TEST_REP_ID, CaseConclusionStatus.PENDING.name());
    }
}
