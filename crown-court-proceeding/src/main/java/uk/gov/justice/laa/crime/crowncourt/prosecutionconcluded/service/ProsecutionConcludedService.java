package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.entity.WQHearingEntity;
import uk.gov.justice.laa.crime.crowncourt.enums.JurisdictionType;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.builder.CaseConclusionDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper.CalculateOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper.OffenceHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper.ReservationsRepositoryHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.impl.ProsecutionConcludedImpl;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import java.util.List;

@Service
@Slf4j
@XRayEnabled
@RequiredArgsConstructor
public class ProsecutionConcludedService {

    private final CalculateOutcomeHelper calculateOutcomeHelper;

    private final ProsecutionConcludedValidator prosecutionConcludedValidator;

    private final ProsecutionConcludedImpl prosecutionConcludedImpl;

    private final ReservationsRepositoryHelper reservationsRepositoryHelper;

    private final CaseConclusionDTOBuilder caseConclusionDTOBuilder;

    private final OffenceHelper offenceHelper;

    private final ProsecutionConcludedDataService prosecutionConcludedDataService;

    private final HearingsService hearingsService;

    private final MaatCourtDataService maatCourtDataService;

    public void execute(final ProsecutionConcluded prosecutionConcluded) {

        log.info("CC Outcome process is kicked off for  maat-id {}", prosecutionConcluded.getMaatId());
        prosecutionConcludedValidator.validateRequestObject(prosecutionConcluded);

        WQHearingEntity wqHearingEntity = maatCourtDataService.retrieveHearingForCaseConclusion(prosecutionConcluded);
        //WQHearingEntity wqHearingEntity = hearingsService.retrieveHearingForCaseConclusion(prosecutionConcluded);
        //TODO
        if (prosecutionConcluded.isConcluded()
                && wqHearingEntity != null
                && JurisdictionType.CROWN.name().equalsIgnoreCase(wqHearingEntity.getWqJurisdictionType())) {

            if (reservationsRepositoryHelper.isMaatRecordLocked(prosecutionConcluded.getMaatId())) {
                prosecutionConcludedDataService.execute(prosecutionConcluded);
            } else {
                executeCCOutCome(prosecutionConcluded, wqHearingEntity);
            }
        }

    }

    public void executeCCOutCome(ProsecutionConcluded prosecutionConcluded, WQHearingEntity wqHearingEntity) {
        List<OffenceSummary> offenceSummaryList = prosecutionConcluded.getOffenceSummary();
        List<OffenceSummary> trialOffences = offenceHelper
                .getTrialOffences(offenceSummaryList, prosecutionConcluded.getMaatId());

        if (!trialOffences.isEmpty()) {
            log.info("Number of Valid offences for CC Outcome Calculations : {}", trialOffences.size());
            processOutcome(prosecutionConcluded, wqHearingEntity, trialOffences);
        }
        prosecutionConcludedDataService.updateConclusion(prosecutionConcluded.getMaatId());
        log.info("CC Outcome is completed for  maat-id {}", prosecutionConcluded.getMaatId());
    }

    private void processOutcome(ProsecutionConcluded prosecutionConcluded, WQHearingEntity wqHearingEntity, List<OffenceSummary> trialOffences) {

        prosecutionConcludedValidator.validateOuCode(wqHearingEntity.getOuCourtLocation());
        String calculatedOutcome = calculateOutcomeHelper.calculate(trialOffences);
        log.info("calculated outcome is {} for this maat-id {}", calculatedOutcome, prosecutionConcluded.getMaatId());

        ConcludedDTO concludedDTO = caseConclusionDTOBuilder.build(prosecutionConcluded, wqHearingEntity, calculatedOutcome);

        prosecutionConcludedImpl.execute(concludedDTO);
    }
}
