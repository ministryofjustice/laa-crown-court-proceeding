package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.ResultCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAPIService;

import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtAppealOutcome.isAppeal;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.caseTypeForAppeal;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.caseTypeForTrial;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.isTrial;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProsecutionConcludedImpl {

    private final CrownCourtCodeHelper crownCourtCodeHelper;

    private final ProcessSentencingImpl processSentencingHelper;

    private final ResultCodeHelper resultCodeHelper;

    private final CourtDataAPIService courtDataAPIService;

    public void execute(ConcludedDTO concludedDTO) {

        Integer maatId = concludedDTO.getProsecutionConcluded().getMaatId();
        final RepOrderDTO repOrderDTO = courtDataAPIService.getRepOrder(maatId);
        if (repOrderDTO != null) {
            log.debug("Maat-id found and processing ProsecutionConcluded");

            verifyCaseTypeValidator(repOrderDTO, concludedDTO.getCalculatedOutcome());

            courtDataAPIService
                    .updateCrownCourtOutcome(
                            UpdateCCOutcome.builder()
                                    .repId(maatId)
                                    .ccOutcome(concludedDTO.getCalculatedOutcome())
                                    .benchWarrantIssued(resultCodeHelper.isBenchWarrantIssued(concludedDTO.getCalculatedOutcome(), concludedDTO.getHearingResultCodeList()))
                                    .appealType(repOrderDTO.getAppealTypeCode())
                                    .imprisoned(resultCodeHelper.isImprisoned(concludedDTO.getCalculatedOutcome(), concludedDTO.getHearingResultCodeList()))
                                    .caseNumber(concludedDTO.getCaseUrn())
                                    .crownCourtCode(crownCourtCodeHelper.getCode(concludedDTO.getOuCourtLocation())).build()
                    );

            processSentencingHelper.processSentencingDate(concludedDTO.getCaseEndDate(), maatId, repOrderDTO.getCatyCaseType());
        }
    }

    private void verifyCaseTypeValidator(RepOrderDTO repOrderDTO, String calculatedOutcome) {

        log.debug("Crown Court - verifying case Type validator");
        String caseType = repOrderDTO.getCatyCaseType();

        if (isTrial(calculatedOutcome) && !caseTypeForTrial(caseType)) {

            throw new ValidationException("Crown Court - Case type not valid for Trial.");
        }

        if (isAppeal(calculatedOutcome) && !caseTypeForAppeal(caseType)) {

            throw new ValidationException("Crown Court  - Case type not valid for Appeal.");
        }
    }

}