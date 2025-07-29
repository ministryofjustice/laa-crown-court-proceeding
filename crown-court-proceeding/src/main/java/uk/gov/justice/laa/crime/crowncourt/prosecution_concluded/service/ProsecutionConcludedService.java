package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder.CaseConclusionDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.enums.CallerType;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CalculateAppealOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CalculateOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.OffenceHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl.ProsecutionConcludedImpl;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;

import java.util.List;
import uk.gov.justice.laa.crime.exception.ValidationException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProsecutionConcludedService {

    private final CalculateOutcomeHelper calculateOutcomeHelper;
    private final CalculateAppealOutcomeHelper calculateAppealOutcomeHelper;
    private final ProsecutionConcludedValidator prosecutionConcludedValidator;
    private final ProsecutionConcludedImpl prosecutionConcludedImpl;
    private final CaseConclusionDTOBuilder caseConclusionDTOBuilder;
    private final OffenceHelper offenceHelper;
    private final ProsecutionConcludedDataService prosecutionConcludedDataService;
    private final CourtDataAPIService courtDataAPIService;
    private final ReactivatedCaseDetectionService reactivatedCaseDetectionService;
    private final CrownCourtCodeHelper crownCourtCodeHelper;

    public void execute(final ProsecutionConcluded prosecutionConcluded) {
        log.info("CC Outcome process is kicked off for  maat-id {}", prosecutionConcluded.getMaatId());
        prosecutionConcludedValidator.validateRequestObject(prosecutionConcluded);

        reactivatedCaseDetectionService.processCase(prosecutionConcluded);

        WQHearingDTO wqHearingDTO = courtDataAPIService.retrieveHearingForCaseConclusion(prosecutionConcluded);

        log.info("wqHearingDTO {}", wqHearingDTO);

        if (wqHearingDTO != null) {
            if (prosecutionConcluded.isConcluded()) {
                if (Boolean.TRUE.equals(courtDataAPIService.isMaatRecordLocked(prosecutionConcluded.getMaatId()))) {
                    log.info("MAAT record is locked for maat-id {}", prosecutionConcluded.getMaatId());
                    prosecutionConcludedDataService.execute(prosecutionConcluded);
                } else {
                    if (JurisdictionType.CROWN.name().equalsIgnoreCase(wqHearingDTO.getWqJurisdictionType())) {
                        prosecutionConcludedValidator.validateOuCode(wqHearingDTO.getOuCourtLocation());
                        executeCCOutCome(prosecutionConcluded, wqHearingDTO, CallerType.QUEUE);
                    } else if (JurisdictionType.MAGISTRATES.name().equalsIgnoreCase(wqHearingDTO.getWqJurisdictionType())
                        && Objects.nonNull(prosecutionConcluded.getApplicationConcluded())) {
                        executeCCOutCome(prosecutionConcluded, wqHearingDTO, CallerType.QUEUE);
                    }
                }
            } else {
                log.info("maat-id {} jurisdiction type: {}", prosecutionConcluded.getMaatId(), wqHearingDTO.getWqJurisdictionType());
                log.info("maat-id {} prosecution is concluded: {}", prosecutionConcluded.getMaatId(), prosecutionConcluded.isConcluded());
            }
        } else {
            log.info("Hearing data is null for maat-id {}", prosecutionConcluded.getMaatId());
            prosecutionConcludedDataService.execute(prosecutionConcluded);
        }
    }

    public void executeCCOutCome(ProsecutionConcluded prosecutionConcluded, WQHearingDTO wqHearingDTO, CallerType callerType) {
        log.info("Processing CC Outcome for maat-id {}", prosecutionConcluded.getMaatId());
        List<OffenceSummary> offenceSummaryList = prosecutionConcluded.getOffenceSummary();

        List<OffenceSummary> trialOffences = offenceHelper
                .getTrialOffences(offenceSummaryList, prosecutionConcluded.getMaatId());

        if (!trialOffences.isEmpty() || Objects.nonNull(prosecutionConcluded.getApplicationConcluded())) {
            log.info("Number of Valid offences for CC Outcome Calculations : {}", trialOffences.size());
            processOutcome(prosecutionConcluded, wqHearingDTO, trialOffences, callerType);
        }
        prosecutionConcludedDataService.updateConclusion(prosecutionConcluded.getMaatId());
        log.info("CC Outcome is completed for  maat-id {}", prosecutionConcluded.getMaatId());
    }

    private void processOutcome(ProsecutionConcluded prosecutionConcluded, WQHearingDTO wqHearingDTO, List<OffenceSummary> trialOffences, CallerType callerType) {
        String crownCourtCode;
        String calculatedOutcome;
        try {
            crownCourtCode = crownCourtCodeHelper.getCode(wqHearingDTO.getOuCourtLocation());
        } catch (ValidationException exception) {
            log.info("Validation exception for maat-id {}: {}", prosecutionConcluded.getMaatId(),exception.getMessage());
            crownCourtCode = null;
        }
        if (Objects.nonNull(prosecutionConcluded.getApplicationConcluded())) {
            calculatedOutcome = calculateAppealOutcomeHelper.calculate(prosecutionConcluded.getApplicationConcluded().getApplicationResultCode());
        } else {
            calculatedOutcome = calculateOutcomeHelper.calculate(trialOffences, prosecutionConcluded, callerType);
        }
        log.info("calculated outcome is {} for this maat-id {}", calculatedOutcome, prosecutionConcluded.getMaatId());

        ConcludedDTO concludedDTO = caseConclusionDTOBuilder.build(prosecutionConcluded, wqHearingDTO, calculatedOutcome, crownCourtCode);
        RepOrderDTO repOrderDTO = courtDataAPIService.getRepOrder(concludedDTO.getProsecutionConcluded().getMaatId());

        if (Objects.isNull(prosecutionConcluded.getApplicationConcluded())) {
            prosecutionConcludedValidator.validateMagsCourtOutcomeExists(repOrderDTO.getMagsOutcome());
        }
        prosecutionConcludedImpl.execute(concludedDTO, repOrderDTO);
    }
}
