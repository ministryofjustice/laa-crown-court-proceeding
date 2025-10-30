package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.APPEAL_CC;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.EITHER_WAY;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.caseTypeForAppeal;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.caseTypeForTrial;

import uk.gov.justice.laa.crime.exception.ValidationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CrownCourtCaseTypeTest {

    @Test
    void givenCaseTypeForTrialIsEmpty_ExceptionThrown() {
        assertThatThrownBy(() -> CrownCourtCaseType.caseTypeForTrial(null)).isInstanceOf(ValidationException.class);
    }

    @ParameterizedTest
    @EnumSource(
            value = CrownCourtCaseType.class,
            names = {"INDICTABLE", "EITHER_WAY", "CC_ALREADY"})
    void givenValidCaseTypeForTrial_returnsTrue(CrownCourtCaseType caseType) {
        assertThat(caseTypeForTrial(caseType.getValue())).isTrue();
    }

    @Test
    void givenCaseTypeForTrialNotValid_ReturnsFalse() {
        assertThat(caseTypeForTrial(APPEAL_CC.getValue())).isFalse();
    }

    @Test
    void givenCaseTypeForAppealIsEmpty_ExceptionThrown() {
        assertThatThrownBy(() -> caseTypeForAppeal(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void givenCaseTypeForAppealIsAppealCC_ReturnsTrue() {
        assertThat(caseTypeForAppeal(APPEAL_CC.getValue())).isTrue();
    }

    @Test
    void givenCaseTypeNotForAppeal_ReturnsFalse() {
        assertThat(caseTypeForAppeal(EITHER_WAY.getValue())).isFalse();
    }
}
