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
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;

import java.util.List;
import java.util.UUID;
import uk.gov.justice.laa.crime.exception.ValidationException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedSchedulerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CourtDataAPIService courtDataAPIService;

    @Mock
    private DeadLetterMessageService deadLetterMessageService;

    @Mock
    private ProsecutionConcludedService prosecutionConcludedService;

    @Mock
    private ProsecutionConcludedRepository prosecutionConcludedRepository;

    @Mock
    private ProsecutionConcludedDataService prosecutionConcludedDataService;

    @InjectMocks
    private ProsecutionConcludedScheduler prosecutionConcludedScheduler;

    @Test
    void givenHearingIsFound_whenSchedulerIsCalled_thenCCtProcessIsTriggered() throws Exception {
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
    void givenHearingIsNotFound_whenSchedulerIsCalled_thenCaseConclusionIsNotProcessIsTriggered() {
        when(prosecutionConcludedRepository.getConcludedCases())
                .thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));

        prosecutionConcludedScheduler.process();

        verify(prosecutionConcludedService, never()).execute(any());
        verify(prosecutionConcludedRepository, never()).saveAll(any());
    }

    @Test
    void givenAJurisdictionIsMagistrates_whenProcessCaseConclusionIsInvoked_thenUpdateConclusionIsSuccess() {
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
    void givenAnInvalidCaseConclusion_whenProcessCaseConclusionIsInvoked_thenUpdateConclusionIsError() {
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
    void givenInvalidCaseData_whenProcessIsInvoked_thenExecuteOutcomeIsFailed() throws Exception {
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
        verify(prosecutionConcludedDataService).execute(any());
    }

    @Test
    void givenAnInvalidOuCode_whenProcessIsInvoked_thenMessageIsSavedAsADeadLetterMessage() {
        WQHearingDTO wqHearingDTO = WQHearingDTO.builder().wqJurisdictionType(JurisdictionType.CROWN.name()).build();
        ProsecutionConcluded prosecutionConcluded = ProsecutionConcluded.builder().maatId(1234).hearingIdWhereChangeOccurred(UUID.randomUUID()).build();

        when(courtDataAPIService.retrieveHearingForCaseConclusion(any())).
            thenReturn(wqHearingDTO);
        when(prosecutionConcludedRepository.getByHearingId(any()))
            .thenReturn(List.of(TestModelDataBuilder.getProsecutionConcludedEntity()));

        doThrow(new ValidationException(ProsecutionConcludedValidator.OU_CODE_LOOKUP_FAILED)).when(prosecutionConcludedService).executeCCOutCome(prosecutionConcluded, wqHearingDTO);

        prosecutionConcludedScheduler.processCaseConclusion(prosecutionConcluded);

        verify(deadLetterMessageService, times(1)).logDeadLetterMessage(
            ProsecutionConcludedValidator.OU_CODE_LOOKUP_FAILED, prosecutionConcluded);
        verify(prosecutionConcludedRepository).saveAll(any());
    }
}
