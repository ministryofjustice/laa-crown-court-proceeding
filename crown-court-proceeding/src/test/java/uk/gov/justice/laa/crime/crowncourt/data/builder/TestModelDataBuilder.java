package uk.gov.justice.laa.crime.crowncourt.data.builder;

import org.springframework.stereotype.Component;
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
    public static final LocalDateTime TEST_WITHDRAWL_DATE =
            LocalDateTime.of(2022, 9, 19, 15, 1, 25);

    public static final String MEANS_ASSESSMENT_TRANSACTION_ID = "7c49ebfe-fe3a-4f2f-8dad-f7b8f03b8327";
    private static final Integer TEST_REP_ID = 91919;

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
                        .withRepType("CROWN")
                        .withRepOrderDecision("PASS")
                        .withWithdrawalDate(TEST_WITHDRAWL_DATE))
                .withIojAppeal(new ApiIOJAppeal()
                        .withIojResult("PASS")
                        .withDecisionResult("PASS"))
                .withFinancialAssessment(new ApiFinancialAssessment()
                        .withInitResult("PASS")
                        .withInitStatus(CurrentStatus.COMPLETE))
                .withPassportAssessment(new ApiPassportAssessment()
                        .withResult("PASS")
                        .withStatus(CurrentStatus.COMPLETE));
    }

    public static ApiCheckCrownCourtActionsResponse getApiCheckCrownCourtActionsResponse(boolean isValid) {
        return new ApiCheckCrownCourtActionsResponse()
                .withRepOrderDecision("")
                .withRepOrderDate(TEST_REP_ORDER_DATE)
                .withRepType("");
    }
}