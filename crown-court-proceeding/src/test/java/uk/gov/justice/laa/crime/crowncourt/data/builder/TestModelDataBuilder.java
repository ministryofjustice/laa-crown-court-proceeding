package uk.gov.justice.laa.crime.crowncourt.data.builder;

import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.model.*;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.*;

import java.time.LocalDateTime;
import java.util.List;

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
    public static final LocalDateTime TEST_SENTENCE_ORDER_DATE =
            LocalDateTime.of(2022, 2, 19, 15, 1, 25);

    public static final String MEANS_ASSESSMENT_TRANSACTION_ID = "7c49ebfe-fe3a-4f2f-8dad-f7b8f03b8327";
    public static final String MOCK_DECISION = "MOCK_DECISION";
    public static final Integer TEST_REP_ID = 91919;
    public static final String TEST_USER = "TEST_USER";
    public static final Integer TEST_APPLICANT_HISTORY_ID = 12449721;

    public static ApiProcessRepOrderRequest getApiProcessRepOrderRequest(boolean isValid) {
        return new ApiProcessRepOrderRequest()
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
                .withDecisionResult("PASS")
                .withAppealTypeCode("Test")
                .withAppealTypeDate(TEST_IOJ_APPEAL_DECISION_DATE);
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

    public static ApiProcessRepOrderResponse getApiProcessRepOrderResponse() {
        return new ApiProcessRepOrderResponse()
                .withRepOrderDecision(MOCK_DECISION)
                .withRepOrderDate(TEST_REP_ORDER_DATE)
                .withRepType("");
    }

    public static CrownCourtDTO getCrownCourtDTO() {
        return CrownCourtDTO.builder()
                .repId(TEST_REP_ID)
                .laaTransactionId(MEANS_ASSESSMENT_TRANSACTION_ID)
                .caseType(CaseType.SUMMARY_ONLY)
                .magCourtOutcome(MagCourtOutcome.APPEAL_TO_CC)
                .decisionDate(TEST_DECISION_DATE)
                .crownCourtSummary(getCrownCourtSummary())
                .passportAssessment(getPassportAssessment())
                .financialAssessment(getFinancialAssessment())
                .dateReceived(TEST_DATE_RECEIVED)
                .iojAppeal(getIojAppeal())
                .isImprisoned(false)
                .userSession(getApiUserSession(true))
                .paymentDetails(getApiPaymentDetails())
                .applicantHistoryId(TEST_APPLICANT_HISTORY_ID)
                .build();
    }

    public static ApiCrownCourtSummary getCrownCourtSummary() {
        return new ApiCrownCourtSummary()
                .withRepOrderDecision(MOCK_DECISION)
                .withRepOrderDate(TEST_REP_ORDER_DATE)
                .withRepId(TEST_REP_ID)
                .withCrownCourtOutcome(List.of(getApiCrownCourtOutcome(CrownCourtOutcome.AQUITTED, TEST_SENTENCE_ORDER_DATE)));
    }

    public static ApiCrownCourtSummary getCrownCourtSummaryWithOutcome(Boolean isImprisoned, List<ApiCrownCourtOutcome> crownCourtOutcomes) {
        return new ApiCrownCourtSummary()
                .withRepOrderDecision(MOCK_DECISION)
                .withRepOrderDate(TEST_REP_ORDER_DATE)
                .withRepId(TEST_REP_ID)
                .withCrownCourtOutcome(crownCourtOutcomes)
                .withIsImprisoned(isImprisoned);
    }

    public static ApiCrownCourtOutcome getApiCrownCourtOutcome(CrownCourtOutcome crownCourtOutcome, LocalDateTime dateSet) {
        return new ApiCrownCourtOutcome()
                .withOutcome(crownCourtOutcome)
                .withDateSet(dateSet)
                .withDescription(crownCourtOutcome.getDescription())
                .withOutComeType(crownCourtOutcome.getType());
    }

    public static IOJAppealDTO getIOJAppealDTO() {
        return IOJAppealDTO.builder()
                .id(1234)
                .repId(TEST_REP_ID)
                .decisionDate(TEST_IOJ_APPEAL_DECISION_DATE)
                .build();
    }

    public static ApiPaymentDetails getApiPaymentDetails() {
        return new ApiPaymentDetails()
                .withPaymentMethod("STANDING ORDER")
                .withBankAccountNo(11101011)
                .withBankAccountName(TEST_USER)
                .withSortCode("121314");
    }

    public static ApiUserSession getApiUserSession(boolean isValid) {
        return new ApiUserSession()
                .withUserName(isValid ? TEST_USER : null)
                .withSessionId("");
    }


    public static ApiUpdateApplicationRequest getApiUpdateApplicationRequest(boolean isValid) {
        return new ApiUpdateApplicationRequest()
                .withRepId(isValid ? TEST_REP_ID : null)
                .withLaaTransactionId(MEANS_ASSESSMENT_TRANSACTION_ID)
                .withApplicantHistoryId(TEST_APPLICANT_HISTORY_ID)
                .withCrownCourtSummary(new ApiCrownCourtSummary()
                        .withRepId(isValid ? TEST_REP_ID : null)
                        .withRepOrderDate(TEST_REP_ORDER_DATE)
                        .withRepType("")
                        .withRepOrderDecision(MOCK_DECISION)
                        .withWithdrawalDate(TEST_WITHDRAWAL_DATE)
                        .withSentenceOrderDate(TEST_SENTENCE_ORDER_DATE))
                .withUserSession(getApiUserSession(isValid))
                .withCaseType(CaseType.EITHER_WAY)
                .withMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL)
                .withDecisionReason(DecisionReason.GRANTED)
                .withCommittalDate(TEST_COMMITTAL_DATE)
                .withDecisionDate(TEST_DECISION_DATE)
                .withDateReceived(TEST_DATE_RECEIVED)
                .withIojAppeal(getIojAppeal())
                .withIsImprisoned(false)
                .withPaymentDetails(getApiPaymentDetails())
                .withFinancialAssessment(getFinancialAssessment())
                .withPassportAssessment(getPassportAssessment());
    }

    public static RepOrderCCOutcomeDTO getRepOrderCCOutcomeDTO(Integer outcomeId, String outcome, LocalDateTime outcomeDate) {

        return RepOrderCCOutcomeDTO.builder()
                .outcome(outcome)
                .outcomeDate(outcomeDate)
                .build();
    }

}