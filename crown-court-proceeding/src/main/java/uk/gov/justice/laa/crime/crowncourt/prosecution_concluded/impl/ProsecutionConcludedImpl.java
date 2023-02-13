package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper.ResultCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtAppealOutcome.isAppeal;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.caseTypeForAppeal;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.caseTypeForTrial;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.isTrial;

@Component
@XRayEnabled
@RequiredArgsConstructor
public class ProsecutionConcludedImpl {

    private final CrownCourtCodeHelper crownCourtCodeHelper;

    private final ProcessSentencingImpl processSentencingHelper;

    private final ResultCodeHelper resultCodeHelper;

    private final MaatCourtDataService maatCourtDataService;

    public void execute(ConcludedDTO concludedDTO) {

        Integer maatId = concludedDTO.getProsecutionConcluded().getMaatId();
        final RepOrderDTO repOrderDTO = maatCourtDataService.getRepOrder(maatId, null);
        if (repOrderDTO != null) {

            verifyCaseTypeValidator(repOrderDTO, concludedDTO.getCalculatedOutcome());

            maatCourtDataService
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

        String caseType = repOrderDTO.getCatyCaseType();

        if (isTrial(calculatedOutcome) && !caseTypeForTrial(caseType)) {

            throw new ValidationException("Crown Court - Case type not valid for Trial.");
        }

        if (isAppeal(calculatedOutcome) && !caseTypeForAppeal(caseType)) {

            throw new ValidationException("Crown Court  - Case type not valid for Appeal.");
        }
    }

}