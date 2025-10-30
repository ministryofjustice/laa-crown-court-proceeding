package uk.gov.justice.laa.crime.crowncourt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.common.model.common.ApiUserSession;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiCrownCourtSummary;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiFinancialAssessment;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiHardshipOverview;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiIOJSummary;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiPassportAssessment;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.DecisionReason;
import uk.gov.justice.laa.crime.enums.FullAssessmentResult;
import uk.gov.justice.laa.crime.enums.InitAssessmentResult;
import uk.gov.justice.laa.crime.enums.PassportAssessmentResult;
import uk.gov.justice.laa.crime.enums.ReviewResult;
import uk.gov.justice.laa.crime.proceeding.MagsDecisionResult;

import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class MagsProceedingServiceTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @InjectMocks
    private MagsProceedingService magsProceedingService;

    @Mock
    private MaatCourtDataService maatCourtDataService;

    @ParameterizedTest
    @MethodSource("getDecisionReasonScenarios")
    void givenDecisionReasonScenario_whenDetermineMagsRepDecisionIsInvoked_thenDecisionReasonIsPersistedAndReturned(
            Scenario scenario, DecisionReason expectedResult) {

        // If we are expecting null, we don't call updateRepOrder
        if (expectedResult != null) {
            when(maatCourtDataService.updateRepOrder(any(UpdateRepOrderRequestDTO.class)))
                    .thenReturn(RepOrderDTO.builder()
                            .dateModified(TestModelDataBuilder.TEST_DATE_MODIFIED)
                            .build());
        }

        CrownCourtDTO crownCourtDTO = buildCrownCourtDTO(scenario);
        MagsDecisionResult decisionResult = magsProceedingService.determineMagsRepDecision(crownCourtDTO);

        if (decisionResult != null) {
            softly.assertThat(decisionResult.getDecisionDate()).isNotNull();
            softly.assertThat(decisionResult.getDecisionReason()).isEqualTo(expectedResult);
            softly.assertThat(decisionResult.getTimestamp()).isEqualTo(TestModelDataBuilder.TEST_DATE_MODIFIED);
        }

        softly.assertThat(crownCourtDTO.getMagsDecisionResult()).isEqualTo(decisionResult);

        if (expectedResult != null) {
            verify(maatCourtDataService).updateRepOrder(any(UpdateRepOrderRequestDTO.class));
        }
    }

    private static CrownCourtDTO buildCrownCourtDTO(Scenario scenario) {
        return CrownCourtDTO.builder()
                .caseType(CaseType.INDICTABLE)
                .userSession(new ApiUserSession().withUserName("user"))
                .crownCourtSummary(new ApiCrownCourtSummary().withIsWarrantIssued(false))
                .iojSummary(new ApiIOJSummary().withIojResult(scenario.iojResult.getResult()))
                .passportAssessment(new ApiPassportAssessment()
                        .withResult(scenario.passportResult() != null ? scenario.passportResult() : null))
                .financialAssessment(new ApiFinancialAssessment()
                        .withInitResult(scenario.initResult() != null ? scenario.initResult.getResult() : null)
                        .withFullResult(scenario.fullResult() != null ? scenario.fullResult.getResult() : null)
                        .withHardshipOverview(new ApiHardshipOverview().withReviewResult(scenario.hardshipResult)))
                .build();
    }

    record Scenario(
            ReviewResult iojResult,
            InitAssessmentResult initResult,
            FullAssessmentResult fullResult,
            ReviewResult hardshipResult,
            String passportResult) {}

    private static Stream<Arguments> getDecisionReasonScenarios() {
        return Stream.of(
                Arguments.of(
                        new Scenario(
                                ReviewResult.PASS,
                                null,
                                null,
                                null,
                                PassportAssessmentResult.FAIL_CONTINUE.getResult()),
                        null),
                Arguments.of(
                        new Scenario(ReviewResult.PASS, null, null, null, PassportAssessmentResult.PASS.getResult()),
                        DecisionReason.GRANTED),
                Arguments.of(new Scenario(ReviewResult.PASS, null, null, null, null), null),
                Arguments.of(
                        new Scenario(ReviewResult.PASS, null, null, null, PassportAssessmentResult.TEMP.getResult()),
                        DecisionReason.GRANTED),
                Arguments.of(
                        new Scenario(ReviewResult.FAIL, null, null, null, PassportAssessmentResult.PASS.getResult()),
                        DecisionReason.FAILIOJ),
                Arguments.of(
                        new Scenario(ReviewResult.PASS, InitAssessmentResult.PASS, null, null, null),
                        DecisionReason.GRANTED),
                Arguments.of(
                        new Scenario(ReviewResult.FAIL, InitAssessmentResult.PASS, null, null, null),
                        DecisionReason.FAILIOJ),
                Arguments.of(
                        new Scenario(ReviewResult.PASS, null, null, ReviewResult.PASS, null), DecisionReason.GRANTED),
                Arguments.of(
                        new Scenario(ReviewResult.FAIL, null, null, ReviewResult.PASS, null), DecisionReason.FAILIOJ),
                Arguments.of(
                        new Scenario(
                                ReviewResult.PASS, InitAssessmentResult.FULL, FullAssessmentResult.PASS, null, null),
                        DecisionReason.GRANTED),
                Arguments.of(
                        new Scenario(
                                ReviewResult.PASS, InitAssessmentResult.FULL, FullAssessmentResult.INEL, null, null),
                        null),
                Arguments.of(
                        new Scenario(
                                ReviewResult.PASS,
                                InitAssessmentResult.FULL,
                                FullAssessmentResult.INEL,
                                ReviewResult.PASS,
                                null),
                        DecisionReason.GRANTED),
                Arguments.of(
                        new Scenario(
                                ReviewResult.PASS,
                                InitAssessmentResult.FULL,
                                FullAssessmentResult.INEL,
                                null,
                                PassportAssessmentResult.FAIL.getResult()),
                        DecisionReason.FAILMEANS),
                Arguments.of(
                        new Scenario(
                                ReviewResult.FAIL, InitAssessmentResult.FULL, FullAssessmentResult.PASS, null, null),
                        DecisionReason.FAILIOJ),
                Arguments.of(
                        new Scenario(ReviewResult.PASS, InitAssessmentResult.FAIL, null, null, null),
                        DecisionReason.FAILMEANS),
                Arguments.of(
                        new Scenario(ReviewResult.FAIL, InitAssessmentResult.FAIL, null, null, null),
                        DecisionReason.FAILMEIOJ),
                Arguments.of(
                        new Scenario(ReviewResult.FAIL, null, null, null, PassportAssessmentResult.FAIL.getResult()),
                        DecisionReason.FAILMEIOJ),
                Arguments.of(
                        new Scenario(ReviewResult.PASS, null, null, null, PassportAssessmentResult.FAIL.getResult()),
                        DecisionReason.FAILMEANS),
                Arguments.of(
                        new Scenario(
                                ReviewResult.FAIL,
                                InitAssessmentResult.FULL,
                                FullAssessmentResult.FAIL,
                                ReviewResult.FAIL,
                                null),
                        DecisionReason.FAILMEIOJ),
                Arguments.of(
                        new Scenario(
                                ReviewResult.PASS,
                                InitAssessmentResult.FULL,
                                FullAssessmentResult.FAIL,
                                ReviewResult.FAIL,
                                null),
                        DecisionReason.FAILMEANS),
                Arguments.of(
                        new Scenario(
                                ReviewResult.PASS, InitAssessmentResult.FULL, FullAssessmentResult.FAIL, null, null),
                        DecisionReason.FAILMEANS));
    }

    @Test
    void givenNullIojResultAndMagsCaseType_whenDetermineMagsRepDecisionIsInvoked_thenReturnNull() {
        // given
        CrownCourtDTO crownCourtDTO = CrownCourtDTO.builder()
                .caseType(CaseType.INDICTABLE)
                .iojSummary(new ApiIOJSummary())
                .build();
        // when
        MagsDecisionResult decisionResult = magsProceedingService.determineMagsRepDecision(crownCourtDTO);
        // then
        assertThat(decisionResult).isNull();
    }

    @Test
    void givenPassedIojResultAndCrownCourtCaseType_whenDetermineMagsRepDecisionIsInvoked_thenReturnNull() {
        // given
        CrownCourtDTO crownCourtDTO = CrownCourtDTO.builder()
                .caseType(CaseType.CC_ALREADY)
                .iojSummary(new ApiIOJSummary().withIojResult("PASS"))
                .build();
        // when
        MagsDecisionResult decisionResult = magsProceedingService.determineMagsRepDecision(crownCourtDTO);
        // then
        assertThat(decisionResult).isNull();
    }

    @Test
    void givenFailedIojAppealResultAndMagsCaseType_whenDetermineMagsRepDecisionIsInvoked_thenReturnNull() {
        // given
        CrownCourtDTO crownCourtDTO = CrownCourtDTO.builder()
                .caseType(CaseType.CC_ALREADY)
                .iojSummary(new ApiIOJSummary().withIojResult("FAIL").withDecisionResult("FAIL"))
                .build();
        // when
        MagsDecisionResult decisionResult = magsProceedingService.determineMagsRepDecision(crownCourtDTO);
        // then
        assertThat(decisionResult).isNull();
    }

    @Test
    void givenNoPassportAssessmentAndNoFinancialAssessment_whenDetermineMagsRepDecisionIsInvoked_thenReturnNull() {
        CrownCourtDTO crownCourtDTO = CrownCourtDTO.builder()
                .caseType(CaseType.INDICTABLE)
                .iojSummary(new ApiIOJSummary().withIojResult("PASS"))
                .build();

        MagsDecisionResult decisionResult = magsProceedingService.determineMagsRepDecision(crownCourtDTO);

        softly.assertThat(decisionResult).isNull();
        softly.assertThat(crownCourtDTO.getMagsDecisionResult()).isNull();

        verify(maatCourtDataService, never()).updateRepOrder(any(UpdateRepOrderRequestDTO.class));
    }
}
