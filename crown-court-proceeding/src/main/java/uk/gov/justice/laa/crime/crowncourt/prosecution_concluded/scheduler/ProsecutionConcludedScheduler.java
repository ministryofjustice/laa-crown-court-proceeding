package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.scheduler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAPIService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

@Slf4j
@Getter
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(value = "feature.prosecution-concluded-schedule.enabled", havingValue = "true")
public class ProsecutionConcludedScheduler {

    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final CourtDataAPIService courtDataAPIService;
    private final ProsecutionConcludedService prosecutionConcludedService;
    private final ProsecutionConcludedRepository prosecutionConcludedRepository;
    private final ProsecutionConcludedDataService prosecutionConcludedDataService;
    private final DeadLetterMessageService deadLetterMessageService;

    @Scheduled(cron = "${queue.message.log.cron.expression}")
    public void process() {

        log.info("Prosecution Conclusion Scheduling is started");

        prosecutionConcludedRepository.getConcludedCases().stream()
                .collect(Collectors.toMap(
                        ProsecutionConcludedEntity::getMaatId, ProsecutionConcludedEntity::getCaseData, (a1, a2) -> a1))
                .values()
                .stream()
                .map(this::convertToObject)
                .filter(Objects::nonNull)
                .forEach(this::processCaseConclusion);

        log.info("Case conclusions are processed");
    }

    public void processCaseConclusion(ProsecutionConcluded prosecutionConcluded) {
        try {
            MDC.put("maatId", String.valueOf(prosecutionConcluded.getMaatId()));
            log.info("Start processing PENDING prosecution concluded.");
            WQHearingDTO wqHearingDTO = courtDataAPIService.retrieveHearingForCaseConclusion(prosecutionConcluded);
            if (wqHearingDTO == null) {
                log.info("Hearing data is not available, retry later.");
                prosecutionConcludedDataService.execute(prosecutionConcluded);
                return;
            }

            if (prosecutionConcluded.isConcluded()) {
                if (isCCConclusion(wqHearingDTO)) {
                    log.info("Hearing data available, CC outcome can now be processed.");
                    prosecutionConcludedService.executeCCOutCome(prosecutionConcluded, wqHearingDTO);
                } else {
                    log.info(
                            "Hearing data available but this is not a crown court case. Marking all records with hearing id {} as PROCESSED.",
                            prosecutionConcluded.getHearingIdWhereChangeOccurred());
                    updateConclusion(
                            prosecutionConcluded
                                    .getHearingIdWhereChangeOccurred()
                                    .toString(),
                            CaseConclusionStatus.PROCESSED);
                }
            } else {
                /*
                Andy Roberts - 23/06/2026
                This might be an error condition, ProsectionConcluded messages are rejected if isConcluded
                is false (see ProsectionConcludedService.execute()), so not sure how they can be added to the PROSECUTION_CONCLUDED table in the first
                place.  So in theory this code block should never be reached.
                */
                log.info("Case is not concluded, CC outcome cannot be processed.");
                prosecutionConcludedDataService.execute(prosecutionConcluded);
            }
        } catch (ValidationException exception) {
            log.error("Prosecution Conclusion failed.");
            deadLetterMessageService.logDeadLetterMessage(exception.getMessage(), prosecutionConcluded);

            updateConclusion(
                    prosecutionConcluded.getHearingIdWhereChangeOccurred().toString(), CaseConclusionStatus.ERROR);
        } catch (Exception exception) {
            log.error("Prosecution Conclusion failed.");
            updateConclusion(
                    prosecutionConcluded.getHearingIdWhereChangeOccurred().toString(), CaseConclusionStatus.ERROR);
        } finally {
            MDC.remove("maatId");
        }
    }

    private boolean isCCConclusion(WQHearingDTO wqHearingDTO) {
        return JurisdictionType.CROWN.name().equalsIgnoreCase(wqHearingDTO.getWqJurisdictionType());
    }

    protected ProsecutionConcluded convertToObject(byte[] caseDate) {
        try {
            return objectMapper.readValue(caseDate, ProsecutionConcluded.class);
        } catch (IOException exception) {
            log.error(exception.getMessage());
            return null;
        }
    }

    public void updateConclusion(String hearingId, CaseConclusionStatus caseConclusionStatus) {
        List<ProsecutionConcludedEntity> processedCases = prosecutionConcludedRepository.getByHearingId(hearingId);
        processedCases.forEach(concludedCase -> {
            concludedCase.setStatus(caseConclusionStatus.name());
            concludedCase.setUpdatedTime(LocalDateTime.now());
        });
        prosecutionConcludedRepository.saveAll(processedCases);
    }
}
