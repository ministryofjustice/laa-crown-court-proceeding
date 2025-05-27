package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.model.Metadata;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder.CaseConclusionDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CalculateOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.OffenceHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl.ProsecutionConcludedImpl;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Plea;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedServiceTest {
    @InjectMocks private ProsecutionConcludedService prosecutionConcludedService;

    @Mock private CalculateOutcomeHelper calculateOutcomeHelper;

    @Mock private CrownCourtCodeHelper crownCourtCodeHelper;

    @Mock private ProsecutionConcludedValidator prosecutionConcludedValidator;

    @Mock private ProsecutionConcludedImpl prosecutionConcludedImpl;

    @Mock private CaseConclusionDTOBuilder caseConclusionDTOBuilder;

    @Mock private OffenceHelper offenceHelper;

    @Mock private ProsecutionConcludedDataService prosecutionConcludedDataService;

    @Mock private CourtDataAPIService courtDataAPIService;

    @Mock private ReactivatedCaseDetectionService reactivatedCaseDetectionService;

    private static final int MAAT_ID = 1212111;

    @Test
    void
            givenMaatRecordIsLocked_whenExecuteIsInvoked_thenMessageIsSavedToProsecutionConcludedRepository() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity());
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(true);

        prosecutionConcludedService.execute(getProsecutionConcluded());

        verify(prosecutionConcludedValidator).validateRequestObject(any());
        verify(courtDataAPIService, atLeast(1)).retrieveHearingForCaseConclusion(any());
        verify(prosecutionConcludedDataService, atLeast(1)).execute(any());
        verify(courtDataAPIService, atLeast(1)).isMaatRecordLocked(anyInt());
        verify(prosecutionConcludedImpl, never()).execute(any(), any());
        verify(calculateOutcomeHelper, never()).calculate(any());

        verify(caseConclusionDTOBuilder, never()).build(any(), any(), any(), any());
        verify(offenceHelper, never()).getTrialOffences(any(), anyInt());
    }

    @Test
    void givenMaatRecordIsNotLocked_whenExecuteIsInvoked_thenMessageIsProcessed() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity());
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(false);
        when(offenceHelper.getTrialOffences(any(), anyInt()))
                .thenReturn(List.of(getOffenceSummary("123")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(
                        ConcludedDTO.builder()
                                .prosecutionConcluded(getProsecutionConcluded())
                                .build());
        when(courtDataAPIService.getRepOrder(any()))
                .thenReturn(RepOrderDTO.builder().magsOutcome("ACQUITTED").build());

        prosecutionConcludedService.execute(getProsecutionConcluded());

        verify(prosecutionConcludedValidator).validateRequestObject(any());
        verify(courtDataAPIService, atLeast(1)).retrieveHearingForCaseConclusion(any());
        verify(courtDataAPIService, atLeast(1)).isMaatRecordLocked(anyInt());
        verify(prosecutionConcludedImpl, atLeast(1)).execute(any(), any());
        verify(calculateOutcomeHelper, atLeast(1)).calculate(any());
        verify(caseConclusionDTOBuilder, atLeast(1)).build(any(), any(), any(), any());
        verify(offenceHelper, atLeast(1)).getTrialOffences(any(), anyInt());
    }

    @Test
    void givenOffenceSummaryListIsEmpty_whenExecuteIsInvoked_thenMessageIsProcessed() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity());
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(false);
        when(offenceHelper.getTrialOffences(any(), anyInt()))
                .thenReturn(List.of(getOffenceSummary("123")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(
                        ConcludedDTO.builder()
                                .prosecutionConcluded(getProsecutionConcluded())
                                .build());
        when(courtDataAPIService.getRepOrder(any()))
                .thenReturn(RepOrderDTO.builder().magsOutcome("ACQUITTED").build());

        prosecutionConcludedService.execute(getProsecutionConcluded());

        verify(prosecutionConcludedValidator).validateRequestObject(any());
        verify(courtDataAPIService).isMaatRecordLocked(anyInt());
        verify(calculateOutcomeHelper).calculate(any());
    }

    @Test
    void givenMessageIsReceived_whenProsecutionConcluded_thenProcessingCCOutcome() {
        ProsecutionConcluded prosecutionConcludedRequest = getProsecutionConcluded();

        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity());
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(false);
        when(offenceHelper.getTrialOffences(any(), anyInt()))
                .thenReturn(List.of(getOffenceSummary("123")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(
                        ConcludedDTO.builder()
                                .prosecutionConcluded(getProsecutionConcluded())
                                .build());
        when(courtDataAPIService.getRepOrder(any()))
                .thenReturn(RepOrderDTO.builder().magsOutcome("ACQUITTED").build());

        prosecutionConcludedService.execute(prosecutionConcludedRequest);

        verify(courtDataAPIService, atLeast(1)).retrieveHearingForCaseConclusion(any());
        verify(prosecutionConcludedValidator).validateRequestObject(any());
        verify(courtDataAPIService).isMaatRecordLocked(anyInt());
        verify(prosecutionConcludedImpl).execute(any(), any());
        verify(calculateOutcomeHelper).calculate(any());
    }

    @Test
    void givenMessageIsReceived_whenCaseIsMeg_thenNotProcess() {
        ProsecutionConcluded prosecutionConcludedRequest = getProsecutionConcluded();

        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(
                        WQHearingDTO.builder()
                                .wqJurisdictionType(JurisdictionType.MAGISTRATES.name())
                                .build());

        prosecutionConcludedService.execute(prosecutionConcludedRequest);

        verify(courtDataAPIService, atLeast(1)).retrieveHearingForCaseConclusion(any());
        verify(prosecutionConcludedValidator)
                .validateRequestObject(any(ProsecutionConcluded.class));
        verify(courtDataAPIService, never()).isMaatRecordLocked(anyInt());
        verify(prosecutionConcludedImpl, never())
                .execute(any(ConcludedDTO.class), any(RepOrderDTO.class));
    }

    @Test
    void givenMessageIsReceived_whenHearingDataNotInMaat_thenTriggerHearingProcessingViaCda() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any())).thenReturn(null);

        prosecutionConcludedService.execute(getProsecutionConcluded());

        verify(prosecutionConcludedValidator).validateRequestObject(any());
        verify(courtDataAPIService, atLeast(1)).retrieveHearingForCaseConclusion(any());
        verify(courtDataAPIService, never()).isMaatRecordLocked(anyInt());
        verify(prosecutionConcludedImpl, never()).execute(any(), any());
        verify(calculateOutcomeHelper, never()).calculate(any());
        verify(caseConclusionDTOBuilder, never()).build(any(), any(), any(), any());
        verify(offenceHelper, never()).getTrialOffences(any(), anyInt());
    }

    private ProsecutionConcluded getProsecutionConcluded() {
        return ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(MAAT_ID)
                .offenceSummary(List.of(getOffenceSummary("OF121")))
                .prosecutionCaseId(UUID.fromString("ce60cac9-ab22-468e-8af9-a3ba2ecece5b"))
                .hearingIdWhereChangeOccurred(
                        UUID.fromString("ce60cac9-ab22-468e-8af9-a3ba2ecece5b"))
                .metadata(Metadata.builder().laaTransactionId(UUID.randomUUID().toString()).build())
                .build();
    }

    private OffenceSummary getOffenceSummary(String offenceCode) {
        return OffenceSummary.builder()
                .offenceCode(offenceCode)
                .proceedingsConcluded(true)
                .plea(Plea.builder().value("GUILTY").build())
                .proceedingsConcludedChangedDate("2012-12-12")
                .build();
    }

    private WQHearingDTO getWQHearingEntity() {
        return WQHearingDTO.builder()
                .maatId(MAAT_ID)
                .caseId(1234)
                .caseUrn("CaseUR")
                .resultCodes("2322,3433")
                .ouCourtLocation("OU")
                .hearingUUID("ce60cac9-ab22-468e-8af9-a3ba2ecece5b")
                .wqJurisdictionType(JurisdictionType.CROWN.name())
                .build();
    }
}
