package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.scheduler;

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
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAPIService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;

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
    private CourtDataAPIService courtDataAPIService;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void givenHearingISFound_whenSchedulerIsCalledThenCCtProcessIsTriggered() throws Exception {
        when(prosecutionConcludedRepository.getConcludedCases())
                .thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));

        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(WQHearingDTO.builder().wqJurisdictionType(JurisdictionType.CROWN.name()).build());

        byte[] caseData = TestModelDataBuilder.getProsecutionConcludedEntity().getCaseData();
        when(objectMapper.readValue(caseData, ProsecutionConcluded.class))
                .thenReturn(ProsecutionConcluded.builder().build());

        prosecutionConcludedScheduler.process();

        verify(prosecutionConcludedService).executeCCOutCome(any(), any());
    }

    @Test
    void givenHearingISNOTFound_whenSchedulerIsCalledThenCaseConclusionIsNotProcessIsTriggered() {
        when(prosecutionConcludedRepository.getConcludedCases())
                .thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));

        prosecutionConcludedScheduler.process();

        verify(prosecutionConcludedService, never()).execute(any());
        verify(prosecutionConcludedRepository, never()).saveAll(any());
    }


    @Test
    void givenAJurisdictionIsMagistrates_whenProcessCaseConclusionIsInvokes_thenUpdateConclusionIsSuccess() {

        when(courtDataAPIService.retrieveHearingForCaseConclusion(any())).
                thenReturn(WQHearingDTO.builder().wqJurisdictionType(JurisdictionType.MAGISTRATES.name()).build());
        when(prosecutionConcludedRepository.getByHearingId(any()))
                .thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));

        prosecutionConcludedScheduler.processCaseConclusion(
                ProsecutionConcluded.builder().maatId(1234).hearingIdWhereChangeOccurred(UUID.randomUUID()).build()
        );

        verify(prosecutionConcludedRepository).saveAll(any());

    }

    @Test
    void givenAInvalidCaseConclusion_whenProcessCaseConclusionIsInvoked_thenUpdateConclusionIsError() {

        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenThrow(new APIClientException());
        when(prosecutionConcludedRepository.getByHearingId(any()))
                .thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));

        prosecutionConcludedScheduler.processCaseConclusion(
                ProsecutionConcluded.builder().maatId(1234).hearingIdWhereChangeOccurred(UUID.randomUUID()).build()
        );
        verify(prosecutionConcludedRepository).saveAll(any());
    }

    @Test
    void givenAInvalidCaseData_whenProcessIsInvoked_thenExecuteOutcomeIsFailed() throws Exception {
        when(objectMapper.readValue(TestModelDataBuilder.getCaseData().getBytes(), ProsecutionConcluded.class))
                .thenReturn(null);
        when(prosecutionConcludedRepository.getConcludedCases())
                .thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any())).
                thenReturn(null);

        byte[] caseData = TestModelDataBuilder.getProsecutionConcludedEntity().getCaseData();
        when(objectMapper.readValue(caseData, ProsecutionConcluded.class))
                .thenReturn(ProsecutionConcluded.builder().build());

        prosecutionConcludedScheduler.process();

        verify(prosecutionConcludedService, times(0)).executeCCOutCome(any(), any());
    }
}
