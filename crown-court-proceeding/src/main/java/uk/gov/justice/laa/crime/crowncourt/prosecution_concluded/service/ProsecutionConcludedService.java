package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.NO_TRIAL_OFFENCES_FOUND;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.UNRECOGNISED_JURISDICTION;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder.CaseConclusionDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CalculateAppealOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CalculateOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.OffenceHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl.ProsecutionConcludedImpl;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

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
    private final DeadLetterMessageService deadLetterMessageService;

    public void execute(final ProsecutionConcluded prosecutionConcluded) {
        log.info("CC Outcome process is kicked off");
        prosecutionConcludedValidator.validateRequestObject(prosecutionConcluded);

        reactivatedCaseDetectionService.processCase(prosecutionConcluded);

        // We are only interested in concluded cases
        if (!prosecutionConcluded.isConcluded()) {
            log.info("Ignoring prosecution concluded message where isConcluded=false");
            return;
        }

        WQHearingDTO wqHearingDTO = courtDataAPIService.retrieveHearingForCaseConclusion(prosecutionConcluded);

        // If no hearing data is available, schedule to retry later
        if (wqHearingDTO == null) {
            log.info("The prosecution is concluded, but no hearing data has been received yet.");
            prosecutionConcludedDataService.execute(prosecutionConcluded);
            return;
        }

        // If the MAAT record is locked, schedule to retry later
        if (Boolean.TRUE.equals(courtDataAPIService.isMaatRecordLocked(prosecutionConcluded.getMaatId()))) {
            log.info("MAAT record is locked");
            prosecutionConcludedDataService.execute(prosecutionConcluded);
            return;
        }

        if (JurisdictionType.CROWN.name().equalsIgnoreCase(wqHearingDTO.getWqJurisdictionType())) {
            prosecutionConcludedValidator.validateOuCode(wqHearingDTO.getOuCourtLocation());
            executeCCOutCome(prosecutionConcluded, wqHearingDTO);
        } else if (JurisdictionType.MAGISTRATES.name().equalsIgnoreCase(wqHearingDTO.getWqJurisdictionType())) {
            if (Objects.nonNull(prosecutionConcluded.getApplicationConcluded())) {
                executeCCOutCome(prosecutionConcluded, wqHearingDTO);
            } else {
                /*
                Andy Roberts 24/06/2026
                Raised this JIRA ticket (https://dsdmoj.atlassian.net/browse/LASB-5107) to handle this case properly, it should be re-tried.
                 */
                log.warn(
                        "Hearing jurisdiction type is MAGISTRATES but applicationConcluded is null, this should be retried but logic is currently missing.");
            }
        } else {
            // This shouldn't happen, but if it does, this validation exception will send the message to the dead letter
            // queue.
            throw new ValidationException(UNRECOGNISED_JURISDICTION);
        }
    }

    public void executeCCOutCome(ProsecutionConcluded prosecutionConcluded, WQHearingDTO wqHearingDTO) {
        log.info("Processing CC Outcome");
        List<OffenceSummary> offenceSummaryList = prosecutionConcluded.getOffenceSummary();

        List<OffenceSummary> trialOffences =
                offenceHelper.getTrialOffences(offenceSummaryList, prosecutionConcluded.getMaatId());

        if (trialOffences.isEmpty()) {
            if (Objects.nonNull(prosecutionConcluded.getApplicationConcluded())) {
                log.info(
                        "No trial offences found but application is concluded, proceeding with CC Outcome calculation");
                processOutcome(prosecutionConcluded, wqHearingDTO, trialOffences);
            } else {
                throw new ValidationException(NO_TRIAL_OFFENCES_FOUND);
            }
        } else {
            log.info("{} trial offences found, proceeding with CC Outcome calculation", trialOffences.size());
            processOutcome(prosecutionConcluded, wqHearingDTO, trialOffences);
        }
        prosecutionConcludedDataService.updateConclusion(prosecutionConcluded.getMaatId());
        log.info("CC Outcome is completed");
    }

    private void processOutcome(
            ProsecutionConcluded prosecutionConcluded, WQHearingDTO wqHearingDTO, List<OffenceSummary> trialOffences) {
        String crownCourtCode;
        String calculatedOutcome;
        try {
            crownCourtCode = crownCourtCodeHelper.getCode(wqHearingDTO.getOuCourtLocation());
        } catch (ValidationException exception) {
            log.info("Validation exception: {}, setting crownCourtCode to null and proceeding", exception.getMessage());
            crownCourtCode = null;
        }
        if (Objects.nonNull(prosecutionConcluded.getApplicationConcluded())) {
            calculatedOutcome = calculateAppealOutcomeHelper.calculate(
                    prosecutionConcluded.getApplicationConcluded().getApplicationResultCode());
        } else {
            calculatedOutcome = calculateOutcomeHelper.calculate(trialOffences);
        }
        ConcludedDTO concludedDTO =
                caseConclusionDTOBuilder.build(prosecutionConcluded, wqHearingDTO, calculatedOutcome, crownCourtCode);
        RepOrderDTO repOrderDTO = courtDataAPIService.getRepOrder(
                concludedDTO.getProsecutionConcluded().getMaatId());

        if (Objects.isNull(prosecutionConcluded.getApplicationConcluded())) {
            if (repOrderDTO.getMagsOutcome() == null) {
                prosecutionConcludedDataService.execute(prosecutionConcluded);
                if (deadLetterMessageService.hasNoDeadLetterMessageForMaatId(
                        prosecutionConcluded.getMaatId(), CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME)) {
                    log.info("Logging dead letter message");
                    prosecutionConcludedValidator.validateMagsCourtOutcomeExists(repOrderDTO.getMagsOutcome());
                }
            }
            log.info("Mags outcome exists");
            prosecutionConcludedValidator.validateIsAppealMissing(repOrderDTO.getCatyCaseType());
        } else {
            log.info("Validating Application Result Code");
            prosecutionConcludedValidator.validateApplicationResultCode(
                    prosecutionConcluded.getApplicationConcluded().getApplicationResultCode());
        }
        log.info("Executing CC Outcome update");
        prosecutionConcludedImpl.execute(concludedDTO, repOrderDTO);
    }
}
