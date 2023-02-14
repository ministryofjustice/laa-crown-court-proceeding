package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.JurisdictionType;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder.CaseConclusionDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CalculateOutcomeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.OffenceHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl.ProsecutionConcludedImpl;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.OffenceSummary;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
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
    private final CaseConclusionDTOBuilder caseConclusionDTOBuilder;
    private final OffenceHelper offenceHelper;
    private final ProsecutionConcludedDataService prosecutionConcludedDataService;
    private final MaatCourtDataService maatCourtDataService;

    public void execute(final ProsecutionConcluded prosecutionConcluded) {

        log.info("CC Outcome process is kicked off for  maat-id {}", prosecutionConcluded.getMaatId());
        prosecutionConcludedValidator.validateRequestObject(prosecutionConcluded);

        WQHearingDTO wqHearingDTO = maatCourtDataService.retrieveHearingForCaseConclusion(prosecutionConcluded);
        if (prosecutionConcluded.isConcluded()
                && wqHearingDTO != null
                && JurisdictionType.CROWN.name().equalsIgnoreCase(wqHearingDTO.getWqJurisdictionType())) {


            if (maatCourtDataService.isMaatRecordLocked(prosecutionConcluded.getMaatId())) {
                prosecutionConcludedDataService.execute(prosecutionConcluded);
            } else {
                executeCCOutCome(prosecutionConcluded, wqHearingDTO);
            }
        }
    }

    public void executeCCOutCome(ProsecutionConcluded prosecutionConcluded, WQHearingDTO wqHearingDTO) {
        List<OffenceSummary> offenceSummaryList = prosecutionConcluded.getOffenceSummary();

        List<OffenceSummary> trialOffences = offenceHelper
                .getTrialOffences(offenceSummaryList, prosecutionConcluded.getMaatId());

        if (!trialOffences.isEmpty()) {
            log.info("Number of Valid offences for CC Outcome Calculations : {}", trialOffences.size());
            processOutcome(prosecutionConcluded, wqHearingDTO, trialOffences);
        }
        prosecutionConcludedDataService.updateConclusion(prosecutionConcluded.getMaatId());
        log.info("CC Outcome is completed for  maat-id {}", prosecutionConcluded.getMaatId());
    }

    private void processOutcome(ProsecutionConcluded prosecutionConcluded, WQHearingDTO wqHearingDTO, List<OffenceSummary> trialOffences) {

        prosecutionConcludedValidator.validateOuCode(wqHearingDTO.getOuCourtLocation());
        String calculatedOutcome = calculateOutcomeHelper.calculate(trialOffences);
        log.info("calculated outcome is {} for this maat-id {}", calculatedOutcome, prosecutionConcluded.getMaatId());

        ConcludedDTO concludedDTO = caseConclusionDTOBuilder.build(prosecutionConcluded, wqHearingDTO, calculatedOutcome);

        prosecutionConcludedImpl.execute(concludedDTO);
    }
}
