package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.commons.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.scheduler.ProsecutionConcludedScheduler;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedSchedulerTest {

    @InjectMocks
    private ProsecutionConcludedScheduler prosecutionConcludedScheduler;
    @Mock
    private ProsecutionConcludedRepository prosecutionConcludedRepository;
    @Mock
    private ProsecutionConcludedService prosecutionConcludedService;
    @Mock
    private MaatCourtDataService maatCourtDataService;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void givenHearingISFound_whenSchedulerIsCalledThenCCtProcessIsTriggered() {
        //given
        when(prosecutionConcludedRepository.getConcludedCases()).thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));
        when(maatCourtDataService.retrieveHearingForCaseConclusion(any())).
                thenReturn(WQHearingDTO.builder().wqJurisdictionType(JurisdictionType.CROWN.name()).build());

        //when
        prosecutionConcludedScheduler.process();

        //then
        verify(prosecutionConcludedService).executeCCOutCome(any(), any());
    }

    @Test
    void givenHearingISNOTFound_whenSchedulerIsCalledThenCaseConclusionIsNotProcessIsTriggered() {
        //given
        when(prosecutionConcludedRepository.getConcludedCases()).thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));

        //when
        prosecutionConcludedScheduler.process();

        //then
        verify(prosecutionConcludedService, never()).execute(any());
        verify(prosecutionConcludedRepository, never()).saveAll(any());
    }


    @Test
    void givenAJurisdictionIsMagistrates_whenProcessCaseConclusionIsInvokes_thenUpdateConclusionIsSuccess() throws Exception {

        when(maatCourtDataService.retrieveHearingForCaseConclusion(any())).
                thenReturn(WQHearingDTO.builder().wqJurisdictionType(JurisdictionType.MAGISTRATES.name()).build());
        when(prosecutionConcludedRepository.getByHearingId(any())).thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));

        prosecutionConcludedScheduler.processCaseConclusion(ProsecutionConcluded.builder().maatId(1234).hearingIdWhereChangeOccurred(UUID.randomUUID()).build());

        verify(prosecutionConcludedRepository).saveAll(any());

    }

    @Test
    void givenAInvalidCaseConclusion_whenProcessCaseConclusionIsInvokes_thenUpdateConclusionIsError() throws Exception {

        when(maatCourtDataService.retrieveHearingForCaseConclusion(any())).thenThrow(new APIClientException());
        when(prosecutionConcludedRepository.getByHearingId(any())).thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));
        prosecutionConcludedScheduler.processCaseConclusion(ProsecutionConcluded.builder().maatId(1234).hearingIdWhereChangeOccurred(UUID.randomUUID()).build());
        verify(prosecutionConcludedRepository).saveAll(any());
    }

    @Test
    void givenAInvalidCaseData_whenProcessIsInvoked_thenExecuteOutcomeIsFailed() throws Exception {
        //given
        when(objectMapper.readValue(TestModelDataBuilder.getCaseData().getBytes(), ProsecutionConcluded.class)).thenThrow(new IOException());
        when(prosecutionConcludedRepository.getConcludedCases()).thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));
        when(maatCourtDataService.retrieveHearingForCaseConclusion(any())).
                thenReturn(null);

        //when
        prosecutionConcludedScheduler.process();

        //then
        verify(prosecutionConcludedService, times(0)).executeCCOutCome(any(), any());
    }
}
