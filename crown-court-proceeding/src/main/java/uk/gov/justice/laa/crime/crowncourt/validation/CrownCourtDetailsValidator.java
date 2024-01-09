package uk.gov.justice.laa.crime.crowncourt.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.justice.laa.crime.enums.CrownCourtOutcome.CONVICTED;
import static uk.gov.justice.laa.crime.enums.CrownCourtOutcome.PART_CONVICTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrownCourtDetailsValidator {

    public static final String MSG_INVALID_CC_OUTCOME = "Imprisoned value cannot be null for Crown Court Outcome of Convicted or Partially Convicted";

    public Optional<Void> validate(final ApiCrownCourtSummary crownCourtSummary) {
        log.info("Validating crown court outcome with repId : {}", crownCourtSummary.getRepId());

        Optional<ApiCrownCourtOutcome> apiCrownCourtOutcome = ofNullable(crownCourtSummary.getCrownCourtOutcome())
                .orElse(emptyList()).stream()
                .filter(outcome -> outcome.getDateSet() == null)
                .findFirst();

        if (apiCrownCourtOutcome.isPresent()) {
            final CrownCourtOutcome crownCourtOutcome = apiCrownCourtOutcome.get().getOutcome();
            log.info("Validating crown court outcome " + crownCourtOutcome);
            if ((crownCourtOutcome.getCode() == CONVICTED.getCode() || crownCourtOutcome.getCode() == PART_CONVICTED.getCode())
                    && crownCourtSummary.getIsImprisoned() == null) {
                throw new ValidationException(MSG_INVALID_CC_OUTCOME);
            }
        }
        return Optional.empty();
    }
}
