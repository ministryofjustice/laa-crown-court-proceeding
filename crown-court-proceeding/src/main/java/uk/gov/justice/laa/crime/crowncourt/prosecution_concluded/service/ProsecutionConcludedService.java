package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder.CaseConclusionDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CalculateOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.OffenceHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl.ProsecutionConcludedImpl;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProsecutionConcludedService {

    private final CalculateOutcomeHelper calculateOutcomeHelper;
    private final ProsecutionConcludedValidator prosecutionConcludedValidator;
    private final ProsecutionConcludedImpl prosecutionConcludedImpl;
    private final CaseConclusionDTOBuilder caseConclusionDTOBuilder;
    private final OffenceHelper offenceHelper;
    private final ProsecutionConcludedDataService prosecutionConcludedDataService;
    private final CourtDataAPIService courtDataAPIService;
    private final ReactivatedCaseDetectionService reactivatedCaseDetectionService;
    private final CrownCourtCodeHelper crownCourtCodeHelper;

    public void execute(final ProsecutionConcluded prosecutionConcluded) {
        log.info(
                "CC Outcome process is kicked off for  maat-id {}",
                prosecutionConcluded.getMaatId());
        prosecutionConcludedValidator.validateRequestObject(prosecutionConcluded);

        reactivatedCaseDetectionService.processCase(prosecutionConcluded);

        WQHearingDTO wqHearingDTO =
                courtDataAPIService.retrieveHearingForCaseConclusion(prosecutionConcluded);

        if (wqHearingDTO != null) {
            if (prosecutionConcluded.isConcluded()
                    && JurisdictionType.CROWN
                            .name()
                            .equalsIgnoreCase(wqHearingDTO.getWqJurisdictionType())) {
                if (Boolean.TRUE.equals(
                        courtDataAPIService.isMaatRecordLocked(prosecutionConcluded.getMaatId()))) {
                    prosecutionConcludedDataService.execute(prosecutionConcluded);
                } else {
                    prosecutionConcludedValidator.validateOuCode(wqHearingDTO.getOuCourtLocation());
                    executeCCOutCome(prosecutionConcluded, wqHearingDTO);
                }
            }
        } else {
            prosecutionConcludedDataService.execute(prosecutionConcluded);
        }
    }

    public void executeCCOutCome(
            ProsecutionConcluded prosecutionConcluded, WQHearingDTO wqHearingDTO) {
        List<OffenceSummary> offenceSummaryList = prosecutionConcluded.getOffenceSummary();

        List<OffenceSummary> trialOffences =
                offenceHelper.getTrialOffences(
                        offenceSummaryList, prosecutionConcluded.getMaatId());

        if (!trialOffences.isEmpty()) {
            log.info(
                    "Number of Valid offences for CC Outcome Calculations : {}",
                    trialOffences.size());
            processOutcome(prosecutionConcluded, wqHearingDTO, trialOffences);
        }
        prosecutionConcludedDataService.updateConclusion(prosecutionConcluded.getMaatId());
        log.info("CC Outcome is completed for  maat-id {}", prosecutionConcluded.getMaatId());
    }

    private void processOutcome(
            ProsecutionConcluded prosecutionConcluded,
            WQHearingDTO wqHearingDTO,
            List<OffenceSummary> trialOffences) {
        String crownCourtCode = crownCourtCodeHelper.getCode(wqHearingDTO.getOuCourtLocation());
        String calculatedOutcome = calculateOutcomeHelper.calculate(trialOffences);
        log.info(
                "calculated outcome is {} for this maat-id {}",
                calculatedOutcome,
                prosecutionConcluded.getMaatId());

        ConcludedDTO concludedDTO =
                caseConclusionDTOBuilder.build(
                        prosecutionConcluded, wqHearingDTO, calculatedOutcome, crownCourtCode);
        RepOrderDTO repOrderDTO =
                courtDataAPIService.getRepOrder(concludedDTO.getProsecutionConcluded().getMaatId());

        prosecutionConcludedValidator.validateMagsCourtOutcomeExists(repOrderDTO.getMagsOutcome());
        prosecutionConcludedImpl.execute(concludedDTO, repOrderDTO);
    }
}
