package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;

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
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void givenHearingISFound_whenSchedulerIsCalledThenCCtProcessIsTriggered() {
        //given
        when(prosecutionConcludedRepository.getConcludedCases()).thenReturn(List.of(ProsecutionConcludedEntity
                .builder()
                .maatId(1234)
                .caseData("test".getBytes(StandardCharsets.UTF_8))
                .build()));
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
        when(prosecutionConcludedRepository.getConcludedCases()).thenReturn(List.of(ProsecutionConcludedEntity
                .builder()
                .maatId(1234)
                .caseData("hearingIdWhereChangeOccurred".getBytes(StandardCharsets.UTF_8))
                .build()));

        //when
        prosecutionConcludedScheduler.process();

        //then
        verify(prosecutionConcludedService, never()).execute(any());
        verify(prosecutionConcludedRepository, never()).saveAll(any());
    }
}
