package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.scheduler.ProsecutionConcludedScheduler;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import java.nio.charset.StandardCharsets;
import java.util.List;

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

    @Test
    void givenHearingISFound_whenSchedulerIsCalledThenCCtProcessIsTriggered() {
        //given
        when(prosecutionConcludedRepository.getConcludedCases()).thenReturn(List.of(ProsecutionConcludedEntity
                .builder()
                .maatId(1234)
                .caseData("test".getBytes(StandardCharsets.UTF_8))
                .build()));
        //when
        when(maatCourtDataService.retrieveHearingForCaseConclusion(any())).
                thenReturn(WQHearingDTO.builder().wqJurisdictionType("CROWN").build());

        prosecutionConcludedScheduler.process();

        //then
        verify(prosecutionConcludedService, atLeast(1)).executeCCOutCome(any(), any());
    }

    @Test
    void givenHearingISNOTFound_whenSchedulerIsCalledThenCaseConclusionIsNotProcessIsTriggered() {
        //given
        when(prosecutionConcludedRepository.getConcludedCases()).thenReturn(List.of(ProsecutionConcludedEntity
                .builder()
                .maatId(1234)
                .caseData("hearingIdWhereChangeOccurred".getBytes(StandardCharsets.UTF_8))
                .build()));

        when(maatCourtDataService.retrieveHearingForCaseConclusion(any())).
                thenReturn(null);
        //when
        prosecutionConcludedScheduler.process();

        //then
        verify(prosecutionConcludedService, never()).execute(any());
        verify(prosecutionConcludedRepository, never()).saveAll(any());
    }
}
