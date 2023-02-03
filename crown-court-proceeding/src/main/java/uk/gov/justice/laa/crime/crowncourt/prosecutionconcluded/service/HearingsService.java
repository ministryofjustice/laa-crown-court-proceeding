package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.client.CourtDataAdapterClient;
import uk.gov.justice.laa.crime.crowncourt.entity.WQHearingEntity;
import uk.gov.justice.laa.crime.crowncourt.exception.MAATCourtDataException;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.WQHearingRepository;

import java.util.List;

@Service
@Slf4j
@XRayEnabled
@RequiredArgsConstructor
public class HearingsService {

    private final WQHearingRepository wqHearingRepository;

    private final CourtDataAdapterClient courtDataAdapterClient;

    private final ProsecutionConcludedDataService prosecutionConcludedDataService;

    public WQHearingEntity retrieveHearingForCaseConclusion(ProsecutionConcluded prosecutionConcluded) {
        //WQHearingEntity hearing = getWqHearingEntity(prosecutionConcluded);
        //TODO

        if (hearing == null && prosecutionConcluded.isConcluded()) {
            triggerHearingDataProcessing(prosecutionConcluded);
            prosecutionConcludedDataService.execute(prosecutionConcluded);
        }

        return hearing;
    }

    private WQHearingEntity getWqHearingEntity(ProsecutionConcluded prosecutionConcluded) {
        List<WQHearingEntity> wqHearingEntityList = wqHearingRepository
                .findByMaatIdAndHearingUUID(prosecutionConcluded.getMaatId(), prosecutionConcluded.getHearingIdWhereChangeOccurred().toString());
        return !wqHearingEntityList.isEmpty() ? wqHearingEntityList.get(0) : null;
    }

    private void triggerHearingDataProcessing(ProsecutionConcluded prosecutionConcluded) {
        try {
            courtDataAdapterClient.triggerHearingProcessing(
                    prosecutionConcluded.getHearingIdWhereChangeOccurred(),
                    prosecutionConcluded.getMetadata().getLaaTransactionId());
        } catch (MAATCourtDataException exception) {
            log.info(exception.getMessage());
        }
    }


}
