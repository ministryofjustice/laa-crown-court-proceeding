package uk.gov.justice.laa.crime.crowncourt.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsResponse;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MagCourtOutcome;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrownCourtProceedingService {

    private final RepOrderService repOrderService;
    private final List<CaseType> caseTypes = List.of(CaseType.INDICTABLE,
            CaseType.CC_ALREADY,
            CaseType.APPEAL_CC,
            CaseType.COMMITAL);
    private final List<MagCourtOutcome> magCourtOutcomes = List.of(MagCourtOutcome.COMMITTED_FOR_TRIAL,
            MagCourtOutcome.SENT_FOR_TRIAL,
            MagCourtOutcome.COMMITTED,
            MagCourtOutcome.APPEAL_TO_CC);

    /*
    PROCEDURE check_crown_court_actions (p_application_object    IN OUT    application_type)
          IS
    begin

       if p_application_object.case_type_object.case_type in ('INDICTABLE','CC ALREADY','APPEAL CC','COMMITAL')
       or (   p_application_object.case_type_object.case_type = 'EITHER WAY'
          and p_application_object.mags_outcome_object.outcome in ('COMMITTED FOR TRIAL'
                                                                        ,'SENT FOR TRIAL'
                                                                        ,'COMMITTED'
                                                                        ,'APPEAL TO CC')
          )
       then
          determine_crown_repord(p_application_object => p_application_object);
       end if;

       capital_and_equity.check_capital_equity_available (p_application_object => p_application_object);

       if p_application_object.passport_assessment_object.status_object.status = 'COMPLETE'
       or (p_application_object.current_assessment_object.fin_assessment_object.initial_assessment_object.status_object.status = 'COMPLETE'
          and ( (  p_application_object.current_assessment_object.fin_assessment_object.full_assessment_object.status_object.status = 'COMPLETE'
                or p_application_object.current_assessment_object.fin_assessment_object.initial_assessment_object.result = 'PASS'
                )
              or (p_application_object.current_assessment_object.fin_assessment_object.initial_assessment_object.result = 'FAIL'
                  and p_application_object.case_type_object.case_type = 'APPEAL CC'
                  )
              )
          )
       then
          contribution.calculate_contribution(p_application_object => p_application_object);
       end if;

       check_crown_court_availability(p_app_obj => p_application_object);

    end check_crown_court_actions;

        PROCEDURE determine_crown_repord (p_application_object    IN OUT    application_type
         ) IS


    BEGIN

        determine_crown_rep_decision (p_app_obj => p_application_object);
        determine_crown_rep_type (p_app_obj => p_application_object);
        determine_crown_reporder_date (p_app_obj => p_application_object);

    END determine_crown_repord;

     */
    public ApiCheckCrownCourtActionsResponse checkCrownCourtActions(CrownCourtsActionsRequestDTO requestDTO) {
        /*
               if p_application_object.case_type_object.case_type in ('INDICTABLE','CC ALREADY','APPEAL CC','COMMITAL')
       or (   p_application_object.case_type_object.case_type = 'EITHER WAY'
          and p_application_object.mags_outcome_object.outcome in ('COMMITTED FOR TRIAL'
                                                                        ,'SENT FOR TRIAL'
                                                                        ,'COMMITTED'
                                                                        ,'APPEAL TO CC')
          )
         */
        if (caseTypes.contains(requestDTO.getCaseType()) ||
                (requestDTO.getCaseType() == CaseType.EITHER_WAY && magCourtOutcomes.contains(requestDTO.getMagCourtOutcome()))) {
            repOrderService.getRepDecision(requestDTO);
            repOrderService.determineCrownRepType(requestDTO);
            ApiCrownCourtSummary apiCrownCourtSummary = repOrderService.determineRepOrderDate(requestDTO);
            return new ApiCheckCrownCourtActionsResponse()
                    .withRepOrderDecision(apiCrownCourtSummary.getRepOrderDecision())
                    .withRepOrderDate(apiCrownCourtSummary.getRepOrderDate())
                    .withRepType(apiCrownCourtSummary.getRepType());
        } else return new ApiCheckCrownCourtActionsResponse();
    }
}
