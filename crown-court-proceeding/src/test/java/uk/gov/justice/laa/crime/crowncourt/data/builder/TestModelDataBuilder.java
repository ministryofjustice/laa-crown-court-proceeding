package uk.gov.justice.laa.crime.crowncourt.data.builder;

import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtApplicationRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.model.*;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.*;

import java.time.LocalDateTime;

@Component
public class TestModelDataBuilder {

    public static final LocalDateTime TEST_COMMITTAL_DATE =
            LocalDateTime.of(2020, 10, 5, 0, 0, 0);
    public static final LocalDateTime TEST_DECISION_DATE = LocalDateTime.of(2021, 6, 5, 15, 0, 0);
    public static final LocalDateTime TEST_DATE_RECEIVED =
            LocalDateTime.of(2022, 10, 9, 15, 1, 25);
    public static final LocalDateTime TEST_REP_ORDER_DATE =
            LocalDateTime.of(2022, 10, 19, 15, 1, 25);
    public static final LocalDateTime TEST_WITHDRAWAL_DATE =
            LocalDateTime.of(2022, 9, 19, 15, 1, 25);
    public static final LocalDateTime TEST_IOJ_APPEAL_DECISION_DATE =
            LocalDateTime.of(2022, 1, 19, 15, 1, 25);

    public static final String MEANS_ASSESSMENT_TRANSACTION_ID = "7c49ebfe-fe3a-4f2f-8dad-f7b8f03b8327";
    public static final String MOCK_DECISION = "MOCK_DECISION";
    public static final Integer TEST_REP_ID = 91919;

    public static ApiCheckCrownCourtActionsRequest getApiCheckCrownCourtActionsRequest(boolean isValid) {
        return new ApiCheckCrownCourtActionsRequest()
                .withRepId(isValid ? TEST_REP_ID : null)
                .withCaseType(CaseType.EITHER_WAY)
                .withMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL)
                .withDecisionReason(DecisionReason.GRANTED)
                .withCommittalDate(TEST_COMMITTAL_DATE)
                .withDecisionDate(TEST_DECISION_DATE)
                .withDateReceived(TEST_DATE_RECEIVED)
                .withLaaTransactionId(MEANS_ASSESSMENT_TRANSACTION_ID)
                .withCrownCourtSummary(new ApiCrownCourtSummary()
                        .withRepId(isValid ? TEST_REP_ID : null)
                        .withRepOrderDate(TEST_REP_ORDER_DATE)
                        .withRepType("")
                        .withRepOrderDecision(MOCK_DECISION)
                        .withWithdrawalDate(TEST_WITHDRAWAL_DATE))
                .withIojAppeal(getIojAppeal())
                .withFinancialAssessment(getFinancialAssessment())
                .withPassportAssessment(getPassportAssessment());
    }

    public static ApiIOJAppeal getIojAppeal() {
        return new ApiIOJAppeal()
                .withIojResult(ReviewResult.FAIL.getResult())
                .withDecisionResult("PASS");
    }

    private static ApiFinancialAssessment getFinancialAssessment() {
        return new ApiFinancialAssessment()
                .withInitResult(InitAssessmentResult.FULL.getResult())
                .withInitStatus(CurrentStatus.COMPLETE)
                .withFullResult(FullAssessmentResult.PASS.getResult())
                .withFullStatus(CurrentStatus.COMPLETE)
                .withHardshipOverview(new ApiHardshipOverview()
                        .withAssessmentStatus(CurrentStatus.COMPLETE)
                        .withReviewResult(ReviewResult.PASS));
    }

    private static ApiPassportAssessment getPassportAssessment() {
        return new ApiPassportAssessment()
                .withResult(PassportAssessmentResult.FAIL.getResult())
                .withStatus(CurrentStatus.COMPLETE);
    }

    public static ApiCheckCrownCourtActionsResponse getApiCheckCrownCourtActionsResponse() {
        return new ApiCheckCrownCourtActionsResponse()
                .withRepOrderDecision(MOCK_DECISION)
                .withRepOrderDate(TEST_REP_ORDER_DATE)
                .withRepType("");
    }

    public static CrownCourtActionsRequestDTO getCrownCourtActionsRequestDTO() {
        return CrownCourtActionsRequestDTO.builder()
                .repId(TEST_REP_ID)
                .caseType(CaseType.SUMMARY_ONLY)
                .magCourtOutcome(MagCourtOutcome.APPEAL_TO_CC)
                .decisionDate(TEST_DECISION_DATE)
                .crownCourtSummary(getCrownCourtSummary())
                .passportAssessment(getPassportAssessment())
                .financialAssessment(getFinancialAssessment())
                .iojAppeal(getIojAppeal())
                .build();
    }

    public static ApiCrownCourtSummary getCrownCourtSummary() {
        return new ApiCrownCourtSummary()
                .withRepOrderDecision(MOCK_DECISION)
                .withRepOrderDate(TEST_REP_ORDER_DATE)
                .withRepId(TEST_REP_ID);
    }

    public static IOJAppealDTO getIOJAppealDTO() {
        return IOJAppealDTO.builder()
                .id(1234)
                .repId(TEST_REP_ID)
                .decisionDate(TEST_IOJ_APPEAL_DECISION_DATE)
                .build();
    }

    public static CrownCourtApplicationRequestDTO getCrownCourtApplicationRequestDTO() {
        return CrownCourtApplicationRequestDTO.builder()
                .repId(TEST_REP_ID)
                .crownCourtSummary(getCrownCourtSummary())
                .build();
    }

}