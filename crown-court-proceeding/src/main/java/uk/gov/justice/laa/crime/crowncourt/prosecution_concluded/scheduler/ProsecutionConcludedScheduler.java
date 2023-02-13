package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.scheduler;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableScheduling
@Getter
@XRayEnabled
@Slf4j
@RequiredArgsConstructor
public class ProsecutionConcludedScheduler {

    private final ProsecutionConcludedRepository prosecutionConcludedRepository;
    private final ProsecutionConcludedService prosecutionConcludedService;
    private final MaatCourtDataService maatCourtDataService;
    private final Gson gson;

    @Scheduled(cron = "${queue.message.log.cron.expression}")
    public void process() {

        log.info("Prosecution Conclusion Scheduling is started");

        prosecutionConcludedRepository.getConcludedCases()
                .stream()
                .collect(Collectors
                        .toMap(ProsecutionConcludedEntity::getMaatId, ProsecutionConcludedEntity::getCaseData, (a1, a2) -> a1))
                .values()
                .stream()
                .map(this::convertToObject)
                .forEach(this::processCaseConclusion);

        log.info("Case conclusions are processed");

    }

    private void processCaseConclusion(ProsecutionConcluded prosecutionConcluded) {
        try {
            WQHearingDTO wqHearingDTO = maatCourtDataService.retrieveHearingForCaseConclusion(prosecutionConcluded);
            if (wqHearingDTO != null) {
                if (isCCConclusion(wqHearingDTO)) {
                    prosecutionConcludedService.executeCCOutCome(prosecutionConcluded, wqHearingDTO);
                } else {
                    updateConclusion(prosecutionConcluded.getHearingIdWhereChangeOccurred().toString(), CaseConclusionStatus.PROCESSED);
                }
            }
        } catch (Exception exception) {
            log.error("Prosecution Conclusion failed for MAAT ID :" + prosecutionConcluded.getMaatId());
            updateConclusion(prosecutionConcluded.getHearingIdWhereChangeOccurred().toString(), CaseConclusionStatus.ERROR);
        }
    }

    private boolean isCCConclusion(WQHearingDTO wqHearingDTO) {
        return JurisdictionType.CROWN.name().equalsIgnoreCase(wqHearingDTO.getWqJurisdictionType());
    }

    private ProsecutionConcluded convertToObject(byte[] caseDate) {
        return gson.fromJson(new String(caseDate, StandardCharsets.UTF_8), ProsecutionConcluded.class);
    }

    @Transactional
    public void updateConclusion(String hearingId, CaseConclusionStatus caseConclusionStatus) {
        List<ProsecutionConcludedEntity> processedCases = prosecutionConcludedRepository.getByHearingId(hearingId);
        processedCases.forEach(concludedCase -> {
            concludedCase.setStatus(caseConclusionStatus.name());
            concludedCase.setUpdatedTime(LocalDateTime.now());
        });
        prosecutionConcludedRepository.saveAll(processedCases);
    }

}
