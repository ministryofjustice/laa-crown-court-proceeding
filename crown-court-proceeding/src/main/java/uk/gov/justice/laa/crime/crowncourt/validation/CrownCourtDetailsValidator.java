package uk.gov.justice.laa.crime.crowncourt.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrownCourtDetailsValidator {

    private static final String CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME = "Cannot have Crown Court outcome without Mags Court outcome";
    private static final String CHECK_CROWN_COURT_DETAILS_IMPRISONED_VALUE_MUST_BE_ENTERED_FOR_CROWN_COURT_OUTCOME = "Check Crown Court Details-Imprisoned value must be entered for Crown Court Outcome of ";
    private static final String CONVICTED_PART_CONVICTED_REGEX = "CONVICTED|PART CONVICTED";

    private final MaatCourtDataService maatCourtDataService;

    public Optional<Void> checkCCDetails(CrownCourtDTO dto) {
        ApiCrownCourtSummary crownCourtSummary = dto.getCrownCourtSummary();
        List<RepOrderCCOutcomeDTO> repOrderCCOutcomeDTOList = maatCourtDataService.getRepOrderCCOutcomeByRepId(dto.getRepId());
        if (null == dto.getMagCourtOutcome() &&
                CollectionUtils.isNotEmpty(repOrderCCOutcomeDTOList) &&
                CaseType.APPEAL_CC != dto.getCaseType()) {
            throw new ValidationException(CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
        }
        if (crownCourtSummary != null && crownCourtSummary.getCrownCourtOutcome() != null
                && !crownCourtSummary.getCrownCourtOutcome().isEmpty()) {
            ApiCrownCourtOutcome crownCourtOutcome = crownCourtSummary.getCrownCourtOutcome()
                    .get(crownCourtSummary.getCrownCourtOutcome().size() - 1);
            if (crownCourtOutcome.getOutcome().getCode().matches(CONVICTED_PART_CONVICTED_REGEX)
                    && dto.getIsImprisoned() == null
                    && crownCourtOutcome.getDateSet() == null
            ) {
                throw new ValidationException(CHECK_CROWN_COURT_DETAILS_IMPRISONED_VALUE_MUST_BE_ENTERED_FOR_CROWN_COURT_OUTCOME
                        + crownCourtOutcome.getOutcome().getDescription());
            }
        }
        return Optional.empty();
    }
}
