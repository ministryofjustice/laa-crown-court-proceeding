package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME;

import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.model.Metadata;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder.CaseConclusionDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CalculateAppealOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CalculateOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.OffenceHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl.ProsecutionConcludedImpl;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ApplicationConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Plea;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedServiceTest {
    @InjectMocks
    private ProsecutionConcludedService prosecutionConcludedService;

    @Mock
    private CalculateOutcomeHelper calculateOutcomeHelper;

    @Mock
    private CrownCourtCodeHelper crownCourtCodeHelper;

    @Mock
    private ProsecutionConcludedValidator prosecutionConcludedValidator;

    @Mock
    private ProsecutionConcludedImpl prosecutionConcludedImpl;

    @Mock
    private CaseConclusionDTOBuilder caseConclusionDTOBuilder;

    @Mock
    private OffenceHelper offenceHelper;

    @Mock
    private ProsecutionConcludedDataService prosecutionConcludedDataService;

    @Mock
    private CourtDataAPIService courtDataAPIService;

    @Mock
    private ReactivatedCaseDetectionService reactivatedCaseDetectionService;

    @Mock
    private CalculateAppealOutcomeHelper calculateAppealOutcomeHelper;

    @Mock
    private DeadLetterMessageService deadLetterMessageService;

    private static final int MAAT_ID = 1212111;

    @Test
    void givenMaatRecordIsLocked_whenExecuteIsInvoked_thenMessageIsSavedToProsecutionConcludedRepository() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity(JurisdictionType.CROWN.name()));
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
                .thenReturn(getWQHearingEntity(JurisdictionType.CROWN.name()));
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(false);
        when(offenceHelper.getTrialOffences(any(), anyInt())).thenReturn(List.of(getOffenceSummary("123")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(ConcludedDTO.builder()
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
                .thenReturn(getWQHearingEntity(JurisdictionType.CROWN.name()));
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(false);
        when(offenceHelper.getTrialOffences(any(), anyInt())).thenReturn(List.of(getOffenceSummary("123")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(ConcludedDTO.builder()
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
                .thenReturn(getWQHearingEntity(JurisdictionType.CROWN.name()));
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(false);
        when(offenceHelper.getTrialOffences(any(), anyInt())).thenReturn(List.of(getOffenceSummary("123")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(ConcludedDTO.builder()
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
    void givenMessageIsReceived_whenCaseIsMegButNotAppeal_thenNotProcess() {
        ProsecutionConcluded prosecutionConcludedRequest = getProsecutionConcluded();

        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(WQHearingDTO.builder()
                        .wqJurisdictionType(JurisdictionType.MAGISTRATES.name())
                        .build());

        prosecutionConcludedService.execute(prosecutionConcludedRequest);

        verify(courtDataAPIService, atLeast(1)).retrieveHearingForCaseConclusion(any());
        verify(prosecutionConcludedValidator).validateRequestObject(any(ProsecutionConcluded.class));
        verify(prosecutionConcludedImpl, never()).execute(any(ConcludedDTO.class), any(RepOrderDTO.class));
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

    @Test
    void givenCrownCourtProsecutionConcludedWithApplicationConcluded_whenExecuteInvoked_thenAppealOutcomeCalculated() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity(JurisdictionType.CROWN.name()));
        when(offenceHelper.getTrialOffences(any(), anyInt())).thenReturn(List.of(getOffenceSummary("123")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(ConcludedDTO.builder()
                        .prosecutionConcluded(getProsecutionConcluded())
                        .build());
        when(courtDataAPIService.getRepOrder(any()))
                .thenReturn(RepOrderDTO.builder().magsOutcome("CONVICTED").build());

        ProsecutionConcluded prosecutionConcludedRequest = getProsecutionConcluded();
        prosecutionConcludedRequest.setApplicationConcluded(getApplicationConcluded());
        prosecutionConcludedService.execute(prosecutionConcludedRequest);

        verify(calculateAppealOutcomeHelper, atLeast(1)).calculate(any());
    }

    @Test
    void givenMagistratesProsecutionConcludedWithApplicationConcluded_whenExecuteInvoked_thenAppealOutcomeCalculated() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity(JurisdictionType.MAGISTRATES.name()));
        when(offenceHelper.getTrialOffences(any(), anyInt())).thenReturn(List.of(getOffenceSummary("123")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(ConcludedDTO.builder()
                        .prosecutionConcluded(getProsecutionConcluded())
                        .build());
        when(courtDataAPIService.getRepOrder(any()))
                .thenReturn(RepOrderDTO.builder().magsOutcome("CONVICTED").build());

        ProsecutionConcluded prosecutionConcludedRequest = getProsecutionConcluded();
        prosecutionConcludedRequest.setApplicationConcluded(getApplicationConcluded());
        prosecutionConcludedService.execute(prosecutionConcludedRequest);

        verify(calculateAppealOutcomeHelper, atLeast(1)).calculate(any());
    }

    @Test
    void givenACaseIsNotConcluded_whenExecuteIsInvoked_thenShouldNotAddToScheduler() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity(JurisdictionType.CROWN.name()));

        ProsecutionConcluded prosecutionConcluded = getProsecutionConcluded();
        prosecutionConcluded.setConcluded(Boolean.FALSE);
        prosecutionConcludedService.execute(prosecutionConcluded);

        verify(prosecutionConcludedValidator).validateRequestObject(any());
        verify(courtDataAPIService, atLeast(1)).retrieveHearingForCaseConclusion(any());
        verify(prosecutionConcludedDataService, never()).execute(any());
        verify(prosecutionConcludedImpl, never()).execute(any(), any());
        verify(calculateOutcomeHelper, never()).calculate(any());
        verify(caseConclusionDTOBuilder, never()).build(any(), any(), any(), any());
        verify(offenceHelper, never()).getTrialOffences(any(), anyInt());
    }

    @Test
    void givenACaseIsNotConcludedAndEmptyHearing_whenExecuteIsInvoked_thenShouldNotAddToScheduler() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any())).thenReturn(null);

        ProsecutionConcluded prosecutionConcluded = getProsecutionConcluded();
        prosecutionConcluded.setConcluded(Boolean.FALSE);
        prosecutionConcludedService.execute(prosecutionConcluded);

        verify(prosecutionConcludedValidator).validateRequestObject(any());
        verify(courtDataAPIService, atLeast(1)).retrieveHearingForCaseConclusion(any());
        verify(prosecutionConcludedDataService, never()).execute(any());
        verify(prosecutionConcludedImpl, never()).execute(any(), any());
        verify(calculateOutcomeHelper, never()).calculate(any());
        verify(caseConclusionDTOBuilder, never()).build(any(), any(), any(), any());
        verify(offenceHelper, never()).getTrialOffences(any(), anyInt());
    }

    @Test
    void whenMessageExistsInDeadLetterQueue_thenExtraValidationIsInvoked() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity(JurisdictionType.CROWN.name()));
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(false);
        when(offenceHelper.getTrialOffences(any(), anyInt())).thenReturn(List.of(getOffenceSummary("OF121")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(ConcludedDTO.builder()
                        .prosecutionConcluded(getProsecutionConcluded())
                        .build());
        when(courtDataAPIService.getRepOrder(any()))
                .thenReturn(RepOrderDTO.builder().magsOutcome(null).build());
        when(calculateOutcomeHelper.calculate(any())).thenReturn("CONVICTED");

        when(deadLetterMessageService.hasNoDeadLetterMessageForMaatId(
                        MAAT_ID, CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME))
                .thenReturn(true);

        prosecutionConcludedService.execute(getProsecutionConcluded());
        verify(prosecutionConcludedDataService, atLeastOnce()).execute(any());
        verify(prosecutionConcludedValidator, atLeastOnce()).validateMagsCourtOutcomeExists(any());
    }

    @Test
    void whenMagsOutcomeIsNotEmpty_thenProsecutionConcludedDataServiceIsNeverInvoked() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity(JurisdictionType.CROWN.name()));
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(false);

        prosecutionConcludedService.execute(getProsecutionConcluded());

        verify(deadLetterMessageService, never()).hasNoDeadLetterMessageForMaatId(any(), any());
        verify(prosecutionConcludedDataService, never()).execute(any());
        verify(prosecutionConcludedValidator, never()).validateMagsCourtOutcomeExists(any());
    }

    @Test
    void whenMessageNotInDeadLetterQueue_thenExtraValidationNotInvoked() {
        when(courtDataAPIService.retrieveHearingForCaseConclusion(any()))
                .thenReturn(getWQHearingEntity(JurisdictionType.CROWN.name()));
        when(courtDataAPIService.isMaatRecordLocked(any())).thenReturn(false);
        when(offenceHelper.getTrialOffences(any(), anyInt())).thenReturn(List.of(getOffenceSummary("OF121")));
        when(caseConclusionDTOBuilder.build(any(), any(), any(), any()))
                .thenReturn(ConcludedDTO.builder()
                        .prosecutionConcluded(getProsecutionConcluded())
                        .build());
        when(courtDataAPIService.getRepOrder(any()))
                .thenReturn(RepOrderDTO.builder().magsOutcome(null).build());
        when(calculateOutcomeHelper.calculate(any())).thenReturn("CONVICTED");
        when(deadLetterMessageService.hasNoDeadLetterMessageForMaatId(
                        MAAT_ID, CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME))
                .thenReturn(false);

        prosecutionConcludedService.execute(getProsecutionConcluded());

        verify(deadLetterMessageService, atLeastOnce())
                .hasNoDeadLetterMessageForMaatId(MAAT_ID, CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
        verify(prosecutionConcludedValidator, never()).validateApplicationResultCode(any());
        verify(prosecutionConcludedImpl, atLeast(1)).execute(any(), any());
    }

    private ProsecutionConcluded getProsecutionConcluded() {
        return ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(MAAT_ID)
                .offenceSummary(List.of(getOffenceSummary("OF121")))
                .prosecutionCaseId(UUID.fromString("ce60cac9-ab22-468e-8af9-a3ba2ecece5b"))
                .hearingIdWhereChangeOccurred(UUID.fromString("ce60cac9-ab22-468e-8af9-a3ba2ecece5b"))
                .metadata(Metadata.builder()
                        .laaTransactionId(UUID.randomUUID().toString())
                        .build())
                .build();
    }

    private ApplicationConcluded getApplicationConcluded() {
        return ApplicationConcluded.builder()
                .applicationId(UUID.fromString("ce60cac9-ab22-468e-8af9-a3ba2ecece5b"))
                .subjectId(UUID.fromString("ce60cac9-ab22-468e-8af9-a3ba2ecece5b"))
                .applicationResultCode("AACA")
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

    private WQHearingDTO getWQHearingEntity(String jurisdictionType) {
        return WQHearingDTO.builder()
                .maatId(MAAT_ID)
                .caseId(1234)
                .caseUrn("CaseUR")
                .resultCodes("2322,3433")
                .ouCourtLocation("OU")
                .hearingUUID("ce60cac9-ab22-468e-8af9-a3ba2ecece5b")
                .wqJurisdictionType(jurisdictionType)
                .build();
    }
}
