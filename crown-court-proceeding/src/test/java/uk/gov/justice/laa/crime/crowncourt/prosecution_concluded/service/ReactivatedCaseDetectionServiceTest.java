package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCase;
import uk.gov.justice.laa.crime.crowncourt.model.Metadata;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Plea;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.ReactivatedProsecutionCaseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactivatedCaseDetectionServiceTest {
    @InjectMocks
    ReactivatedCaseDetectionService reactivatedCaseDetectionService;
    @Mock
    private CourtDataAPIService maatCourtDataService;
    @Mock
    private ReactivatedProsecutionCaseRepository reactivatedProsecutionCaseRepository;

    @Test
    void givenCaseConcludedIsFalseAndPreviousCCOutcomeExists_whenGetProsecutionConcluded_thenRecordIsCreated() {

        ProsecutionConcluded prosecutionConcludedRequest = getProsecutionConcluded(false);
        Integer maatId = prosecutionConcludedRequest.getMaatId();

        when(reactivatedProsecutionCaseRepository.existsByMaatIdAndReportingStatus(maatId, "PENDING"))
                .thenReturn(false);
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(maatId))
                .thenReturn(getPreviousOutComes(maatId));

        reactivatedCaseDetectionService.processCase(prosecutionConcludedRequest);

        verify(reactivatedProsecutionCaseRepository, atLeast(1)).saveAndFlush(any());
    }

    @Test
    void givenCaseConcludedIsTrueAndReactivatedCaseExists_whenGetProsecutionConcluded_thenRecordIsUpdated() {

        ProsecutionConcluded prosecutionConcludedRequest = getProsecutionConcluded(true);
        Integer maatId = prosecutionConcludedRequest.getMaatId();
        ReactivatedProsecutionCase reactivatedProsecutionCase = getReactivatedProsecutionCase(maatId);

        when(reactivatedProsecutionCaseRepository.findByMaatIdAndReportingStatus(maatId, "PENDING"))
                .thenReturn(Optional.of(reactivatedProsecutionCase));

        reactivatedCaseDetectionService.processCase(prosecutionConcludedRequest);
        reactivatedProsecutionCase.setReportingStatus("SUPERSEDED");

        verify(reactivatedProsecutionCaseRepository, atLeast(1)).saveAndFlush(reactivatedProsecutionCase);
    }

    @Test
    void givenCaseConcludedIsFalseAndPreviousCCOutcomeDoesNotExist_whenGetProsecutionConcluded_thenRecordNotCreated() {
        ProsecutionConcluded prosecutionConcludedRequest = getProsecutionConcluded(false);

        Integer maatId = prosecutionConcludedRequest.getMaatId();

        when(reactivatedProsecutionCaseRepository.existsByMaatIdAndReportingStatus(maatId, "PENDING"))
                .thenReturn(false);
        when(maatCourtDataService.getRepOrderCCOutcomeByRepId(maatId))
                .thenReturn(List.of());

        reactivatedCaseDetectionService.processCase(prosecutionConcludedRequest);

        verify(reactivatedProsecutionCaseRepository, times(0)).saveAndFlush(any());
    }

    private ProsecutionConcluded getProsecutionConcluded(boolean isConcluded) {
        return ProsecutionConcluded.builder()
                .isConcluded(isConcluded)
                .maatId(1221)
                .offenceSummary(List.of(getOffenceSummary()))
                .prosecutionCaseId(UUID.fromString("ce60cac9-ab22-468e-8af9-a3ba2ecece5b"))
                .hearingIdWhereChangeOccurred(UUID.fromString("ce60cac9-ab22-468e-8af9-a3ba2ecece5b"))
                .metadata(Metadata.builder().laaTransactionId(UUID.randomUUID().toString()).build())
                .build();
    }

    private OffenceSummary getOffenceSummary() {
        return OffenceSummary.builder()
                .offenceCode("OF121")
                .proceedingsConcluded(true)
                .plea(Plea.builder().value("GUILTY").build())
                .proceedingsConcludedChangedDate("2012-12-12")
                .build();
    }

    private List<RepOrderCCOutcomeDTO> getPreviousOutComes(Integer maatId) {
        return List.of(RepOrderCCOutcomeDTO
                .builder()
                .repId(maatId)
                .caseNumber("Mock-CaseID")
                .outcome("Mock-Outcome")
                .outcomeDate(LocalDateTime.of(2024, 5, 5, 5, 5))
                .build());
    }

    private ReactivatedProsecutionCase getReactivatedProsecutionCase(Integer maatId) {
        return ReactivatedProsecutionCase
                .builder()
                .maatId(maatId)
                .hearingId("ce60cac9-ab22-468e-8af9-a3ba2ecece5b")
                .caseUrn("Mock-CaseID")
                .previousOutcome("Mock-Outcome")
                .previousOutcomeDate(LocalDateTime.of(2024, 5, 5, 5, 5))
                .dateOfStatusChange(LocalDateTime.now())
                .reportingStatus("PENDING")
                .build();
    }
}
