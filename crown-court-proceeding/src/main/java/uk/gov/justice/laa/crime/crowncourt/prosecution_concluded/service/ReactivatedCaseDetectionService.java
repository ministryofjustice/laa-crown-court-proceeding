package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCase;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.ReactivatedProsecutionCaseRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactivatedCaseDetectionService {

    private static final String PENDING = "PENDING";
    private static final String SUPERSEDED = "SUPERSEDED";
    private final CourtDataAPIService courtDataAPIService;
    private final ReactivatedProsecutionCaseRepository reactivatedProsecutionCaseRepository;

    public void processCase(ProsecutionConcluded prosecutionConcluded) {
        if (!prosecutionConcluded.isConcluded()) {
            boolean isCaseRecordExist = reactivatedProsecutionCaseRepository.existsByMaatIdAndReportingStatus(
                    prosecutionConcluded.getMaatId(), PENDING);
            if (!isCaseRecordExist) {
                createReactivatedCaseRecord(prosecutionConcluded);
            }
        } else {
            updateReactivatedCaseRecord(prosecutionConcluded);
        }
    }

    private void createReactivatedCaseRecord(ProsecutionConcluded prosecutionConcluded) {
        Integer maatId = prosecutionConcluded.getMaatId();
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeList = courtDataAPIService.getRepOrderCCOutcomeByRepId(maatId);

        if (!CollectionUtils.isEmpty(repOrderCCOutcomeList)) {
            RepOrderCCOutcomeDTO repOrderCCOutcome = repOrderCCOutcomeList.stream()
                    .max(Comparator.comparingInt(RepOrderCCOutcomeDTO::getId))
                    .get();

            ReactivatedProsecutionCase createReactivatedCase =
                    buildCreateReactivatedCase(prosecutionConcluded, repOrderCCOutcome);
            reactivatedProsecutionCaseRepository.saveAndFlush(createReactivatedCase);
            log.info("Created reactivated case for MAAT ID - {}", maatId);
        }
    }

    private ReactivatedProsecutionCase buildCreateReactivatedCase(
            ProsecutionConcluded prosecutionConcluded, RepOrderCCOutcomeDTO repOrderCCOutcome) {
        return ReactivatedProsecutionCase.builder()
                .maatId(prosecutionConcluded.getMaatId())
                .hearingId(String.valueOf(prosecutionConcluded.getHearingIdWhereChangeOccurred()))
                .caseUrn(repOrderCCOutcome.getCaseNumber())
                .previousOutcome(repOrderCCOutcome.getOutcome())
                .previousOutcomeDate(repOrderCCOutcome.getOutcomeDate())
                .dateOfStatusChange(LocalDateTime.now())
                .reportingStatus(PENDING)
                .build();
    }

    private void updateReactivatedCaseRecord(ProsecutionConcluded prosecutionConcluded) {
        Integer maatId = prosecutionConcluded.getMaatId();
        Optional<ReactivatedProsecutionCase> reactivatedProsecutionCase =
                reactivatedProsecutionCaseRepository.findByMaatIdAndReportingStatus(maatId, PENDING);
        if (reactivatedProsecutionCase.isPresent()) {
            ReactivatedProsecutionCase updateReactivatedCase = reactivatedProsecutionCase.get();
            updateReactivatedCase.setReportingStatus(SUPERSEDED);
            reactivatedProsecutionCaseRepository.saveAndFlush(updateReactivatedCase);
            log.info("Updated report status as SUPERSEDED for MAAT ID - {}", maatId);
        }
    }
}
