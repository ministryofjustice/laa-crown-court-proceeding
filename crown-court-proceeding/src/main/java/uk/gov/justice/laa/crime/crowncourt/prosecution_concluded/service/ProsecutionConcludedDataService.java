package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProsecutionConcludedDataService {

    private final ObjectMapper objectMapper;
    private final ProsecutionConcludedRepository prosecutionConcludedRepository;

    @Transactional
    public void execute(final ProsecutionConcluded prosecutionConcluded) {

        Integer maatId = prosecutionConcluded.getMaatId();
        log.info("Scheduling MAAT -ID {} for later processing", maatId);

        List<ProsecutionConcludedEntity> prosecutionConcludedEntityList =
                prosecutionConcludedRepository.getByMaatId(maatId);
        if (prosecutionConcludedEntityList.isEmpty()) {
            try {
                ProsecutionConcludedEntity prosecutionConcludedEntity = build(prosecutionConcluded, maatId);
                prosecutionConcludedRepository.save(prosecutionConcludedEntity);
            } catch (JsonProcessingException exception) {
                log.error(exception.toString());
            }
        } else {
            prosecutionConcludedEntityList.forEach(entity -> {
                entity.setRetryCount(entity.getRetryCount() + 1);
                entity.setUpdatedTime(LocalDateTime.now());
            });
            prosecutionConcludedRepository.saveAll(prosecutionConcludedEntityList);
        }
        log.info("MAAT -ID {} scheduling is complete", maatId);
    }

    private ProsecutionConcludedEntity build(ProsecutionConcluded prosecutionConcluded, Integer maatId)
            throws JsonProcessingException {
        return ProsecutionConcludedEntity.builder()
                .maatId(maatId)
                .hearingId(
                        prosecutionConcluded.getHearingIdWhereChangeOccurred().toString())
                .caseData(convertAsByte(prosecutionConcluded))
                .status(CaseConclusionStatus.PENDING.name())
                .createdTime(LocalDateTime.now())
                .retryCount(0)
                .updatedTime(LocalDateTime.now())
                .build();
    }

    private byte[] convertAsByte(final ProsecutionConcluded message) throws JsonProcessingException {
        return Optional.ofNullable(message).isPresent() ? objectMapper.writeValueAsBytes(message) : null;
    }

    @Transactional
    public void updateConclusion(Integer maatId) {
        List<ProsecutionConcludedEntity> processedCases = prosecutionConcludedRepository.getByMaatId(maatId);
        processedCases.forEach(concludedCase -> {
            concludedCase.setStatus(CaseConclusionStatus.PROCESSED.name());
            concludedCase.setUpdatedTime(LocalDateTime.now());
        });
        prosecutionConcludedRepository.saveAll(processedCases);
    }

    public long getCountByMaatIdAndStatus(Integer maatId, String status) {
        return prosecutionConcludedRepository.countByMaatIdAndStatus(maatId, status);
    }
}
