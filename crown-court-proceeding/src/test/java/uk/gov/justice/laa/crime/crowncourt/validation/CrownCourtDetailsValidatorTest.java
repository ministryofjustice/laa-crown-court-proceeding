package uk.gov.justice.laa.crime.crowncourt.validation;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CrownCourtDetailsValidatorTest {

    private final CrownCourtDetailsValidator crownCourtDetailsValidator = new CrownCourtDetailsValidator();

    @Test
    public void givenValidCrownCourtOutcomeConvicted_whenValidateIsInvoked_thenValidationPasses() {
        ApiCrownCourtOutcome crownCourtOutcome = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.CONVICTED, LocalDateTime.now());
        ApiCrownCourtSummary crownCourtSummary = TestModelDataBuilder.getCrownCourtSummaryWithOutcome(true, Arrays.asList(crownCourtOutcome));

        assertThat(crownCourtDetailsValidator.validate(crownCourtSummary)).isEmpty();
    }

    @Test
    public void givenValidCrownCourtOutcomePartConvicted_whenValidateIsInvoked_thenValidationPasses() {
        ApiCrownCourtOutcome crownCourtOutcome = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.PART_CONVICTED, LocalDateTime.now());
        ApiCrownCourtSummary crownCourtSummary = TestModelDataBuilder.getCrownCourtSummaryWithOutcome(true, Arrays.asList(crownCourtOutcome));

        assertThat(crownCourtDetailsValidator.validate(crownCourtSummary)).isEmpty();
    }

    @Test
    public void givenLatestCrownCourtOutcomeConvictedWithNullDateAndNullImprisoned_whenValidateIsInvoked_thenValidationFails() {
        ApiCrownCourtOutcome crownCourtOutcome1 = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.AQUITTED, LocalDateTime.now().minusDays(2));
        ApiCrownCourtOutcome crownCourtOutcome2 = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.SUCCESSFUL, LocalDateTime.now().minusDays(1));
        ApiCrownCourtOutcome crownCourtOutcome3 = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.CONVICTED, null);
        ApiCrownCourtSummary crownCourtSummary = TestModelDataBuilder.getCrownCourtSummaryWithOutcome(null, Arrays.asList(crownCourtOutcome1, crownCourtOutcome2, crownCourtOutcome3));

        assertThatThrownBy(() -> crownCourtDetailsValidator.validate(crownCourtSummary))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(CrownCourtDetailsValidator.MSG_INVALID_CC_OUTCOME);
    }

    @Test
    public void givenLatestCrownCourtOutcomeConvicted_whenValidateIsInvoked_thenValidationPasses() {
        ApiCrownCourtOutcome crownCourtOutcome1 = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.AQUITTED, LocalDateTime.now().minusDays(2));
        ApiCrownCourtOutcome crownCourtOutcome2 = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.SUCCESSFUL, LocalDateTime.now().minusDays(1));
        ApiCrownCourtOutcome crownCourtOutcome3 = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.CONVICTED, null);
        ApiCrownCourtSummary crownCourtSummary = TestModelDataBuilder.getCrownCourtSummaryWithOutcome(false, Arrays.asList(crownCourtOutcome1, crownCourtOutcome2, crownCourtOutcome3));

        assertThat(crownCourtDetailsValidator.validate(crownCourtSummary)).isEmpty();
    }

    @Test
    public void givenLatestCrownCourtOutcomeSuccessfulWithNullDate_whenValidateIsInvoked_thenValidationPasses() {
        ApiCrownCourtOutcome crownCourtOutcome1 = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.AQUITTED, LocalDateTime.now().minusDays(2));
        ApiCrownCourtOutcome crownCourtOutcome2 = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.CONVICTED, LocalDateTime.now().minusDays(1));
        ApiCrownCourtOutcome crownCourtOutcome3 = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.SUCCESSFUL, null);
        ApiCrownCourtSummary crownCourtSummary = TestModelDataBuilder.getCrownCourtSummaryWithOutcome(null, Arrays.asList(crownCourtOutcome1, crownCourtOutcome2, crownCourtOutcome3));

        assertThat(crownCourtDetailsValidator.validate(crownCourtSummary)).isEmpty();
    }

    @Test
    public void givenValidCrownCourtOutcomeSuccessful_whenValidateIsInvoked_thenValidationPasses() {
        ApiCrownCourtOutcome crownCourtOutcome = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.SUCCESSFUL, LocalDateTime.now());
        ApiCrownCourtSummary crownCourtSummary = TestModelDataBuilder.getCrownCourtSummaryWithOutcome(false, Arrays.asList(crownCourtOutcome));

        assertThat(crownCourtDetailsValidator.validate(crownCourtSummary)).isEmpty();
    }

    @Test
    public void givenValidCrownCourtOutcomeWithNullDate_whenValidateIsInvoked_thenValidationPasses() {
        ApiCrownCourtOutcome crownCourtOutcome = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.SUCCESSFUL, null);
        ApiCrownCourtSummary crownCourtSummary = TestModelDataBuilder.getCrownCourtSummaryWithOutcome(null, Arrays.asList(crownCourtOutcome));

        assertThat(crownCourtDetailsValidator.validate(crownCourtSummary)).isEmpty();
    }

    @Test
    public void givenCrownCourtOutcomeConvictedWithNullDateAndNullImprisoned_whenValidateIsInvoked_thenValidationFails() {
        ApiCrownCourtOutcome crownCourtOutcome = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.CONVICTED, null);
        ApiCrownCourtSummary crownCourtSummary = TestModelDataBuilder.getCrownCourtSummaryWithOutcome(null, Arrays.asList(crownCourtOutcome));

        assertThatThrownBy(() -> crownCourtDetailsValidator.validate(crownCourtSummary))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(CrownCourtDetailsValidator.MSG_INVALID_CC_OUTCOME);
    }

    @Test
    public void givenCrownCourtOutcomePartConvictedWithNullDateAndNullImprisoned_whenValidateIsInvoked_thenValidationFails() {
        ApiCrownCourtOutcome crownCourtOutcome = TestModelDataBuilder.getApiCrownCourtOutcome(CrownCourtOutcome.PART_CONVICTED, null);
        ApiCrownCourtSummary crownCourtSummary = TestModelDataBuilder.getCrownCourtSummaryWithOutcome(null, Arrays.asList(crownCourtOutcome));

        assertThatThrownBy(() -> crownCourtDetailsValidator.validate(crownCourtSummary))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(CrownCourtDetailsValidator.MSG_INVALID_CC_OUTCOME);
    }

}
