package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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

@Slf4j
@Getter
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "feature.prosecution-concluded-schedule.enabled",
        havingValue = "true")
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
                .collect(
                        Collectors.toMap(
                                ProsecutionConcludedEntity::getMaatId,
                                ProsecutionConcludedEntity::getCaseData,
                                (a1, a2) -> a1))
                .values()
                .stream()
                .map(this::convertToObject)
                .filter(Objects::nonNull)
                .forEach(this::processCaseConclusion);

        log.info("Case conclusions are processed");
    }

    public void processCaseConclusion(ProsecutionConcluded prosecutionConcluded) {
        try {
            WQHearingDTO hearing =
                    courtDataAPIService.retrieveHearingForCaseConclusion(prosecutionConcluded);
            if (hearing != null) {
                if (isCCConclusion(hearing)) {
                    prosecutionConcludedService.executeCCOutCome(prosecutionConcluded, hearing);
                } else {
                    updateConclusion(
                            prosecutionConcluded.getHearingIdWhereChangeOccurred().toString(),
                            CaseConclusionStatus.PROCESSED);
                }
            } else {
                prosecutionConcludedDataService.execute(prosecutionConcluded);
            }
        } catch (ValidationException exception) {
            log.error(
                    "Prosecution Conclusion failed for MAAT ID :"
                            + prosecutionConcluded.getMaatId());
            deadLetterMessageService.logDeadLetterMessage(
                    exception.getMessage(), prosecutionConcluded);

            updateConclusion(
                    prosecutionConcluded.getHearingIdWhereChangeOccurred().toString(),
                    CaseConclusionStatus.ERROR);
        } catch (Exception exception) {
            log.error(
                    "Prosecution Conclusion failed for MAAT ID :"
                            + prosecutionConcluded.getMaatId());
            updateConclusion(
                    prosecutionConcluded.getHearingIdWhereChangeOccurred().toString(),
                    CaseConclusionStatus.ERROR);
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
        List<ProsecutionConcludedEntity> processedCases =
                prosecutionConcludedRepository.getByHearingId(hearingId);

        processedCases.forEach(
                concludedCase -> {
                    concludedCase.setStatus(caseConclusionStatus.name());
                    concludedCase.setUpdatedTime(LocalDateTime.now());
                });

        prosecutionConcludedRepository.saveAll(processedCases);
    }
}
