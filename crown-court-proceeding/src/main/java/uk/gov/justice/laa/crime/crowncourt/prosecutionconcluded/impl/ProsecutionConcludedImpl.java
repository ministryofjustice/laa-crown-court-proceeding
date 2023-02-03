package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.impl;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.entity.RepOrderEntity;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.dto.ConcludedDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper.CrownCourtCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper.ResultCodeHelper;
import uk.gov.justice.laa.crime.crowncourt.repository.CrownCourtStoredProcedureRepository;
import uk.gov.justice.laa.crime.crowncourt.repository.RepOrderRepository;

import java.util.Optional;

import static uk.gov.justice.laa.crime.crowncourt.enums.CrownCourtAppealOutcome.isAppeal;
import static uk.gov.justice.laa.crime.crowncourt.enums.CrownCourtCaseType.caseTypeForAppeal;
import static uk.gov.justice.laa.crime.crowncourt.enums.CrownCourtCaseType.caseTypeForTrial;
import static uk.gov.justice.laa.crime.crowncourt.enums.CrownCourtTrialOutcome.isTrial;

@Component
@XRayEnabled
@RequiredArgsConstructor
public class ProsecutionConcludedImpl {

    private final RepOrderRepository repOrderRepository;

    private final CrownCourtStoredProcedureRepository crownCourtStoredProcedureRepository;

    private final CrownCourtCodeHelper crownCourtCodeHelper;

    private final ProcessSentencingImpl processSentencingHelper;

    private final ResultCodeHelper resultCodeHelper;

    public void execute(ConcludedDTO concludedDTO) {

        Integer maatId = concludedDTO.getProsecutionConcluded().getMaatId();
        final Optional<RepOrderEntity> optionalRepEntity = repOrderRepository.findById(maatId);
        //TODO
        if (optionalRepEntity.isPresent()) {

            RepOrderEntity repOrderEntity = optionalRepEntity.get();

            verifyCaseTypeValidator(repOrderEntity, concludedDTO.getCalculatedOutcome());

            crownCourtStoredProcedureRepository
                    .updateCrownCourtOutcome(
                            maatId,
                            concludedDTO.getCalculatedOutcome(),
                            resultCodeHelper.isBenchWarrantIssued(concludedDTO.getCalculatedOutcome(), concludedDTO.getHearingResultCodeList()),
                            repOrderEntity.getAppealTypeCode(),
                            resultCodeHelper.isImprisoned(concludedDTO.getCalculatedOutcome(), concludedDTO.getHearingResultCodeList()),
                            concludedDTO.getCaseUrn(),
                            crownCourtCodeHelper.getCode(concludedDTO.getOuCourtLocation()));

            processSentencingHelper.processSentencingDate(concludedDTO.getCaseEndDate(), maatId, repOrderEntity.getCatyCaseType());
        }
    }

    private void verifyCaseTypeValidator(RepOrderEntity repOrder, String calculatedOutcome) {

        String caseType = repOrder.getCatyCaseType();

        if (isTrial(calculatedOutcome) && !caseTypeForTrial(caseType)) {

            throw new ValidationException("Crown Court - Case type not valid for Trial.");
        }

        if (isAppeal(calculatedOutcome) && !caseTypeForAppeal(caseType)) {

            throw new ValidationException("Crown Court  - Case type not valid for Appeal.");
        }
    }

}