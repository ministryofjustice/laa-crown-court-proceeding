package uk.gov.justice.laa.crime.crowncourt.data.builder;

import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.*;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.model.*;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.*;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.*;
import uk.gov.justice.laa.crime.enums.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    public static final LocalDateTime TEST_DATE_MODIFIED = LocalDateTime.now();

    public static final String MEANS_ASSESSMENT_TRANSACTION_ID = "7c49ebfe-fe3a-4f2f-8dad-f7b8f03b8327";
    public static final String MOCK_DECISION = "MOCK_DECISION";
    public static final Integer TEST_REP_ID = 91919;
    public static final String TEST_USER = "TEST_USER";
    public static final Integer TEST_APPLICANT_HISTORY_ID = 12449721;

    public static final Integer TEST_CASE_ID = 45673;
    public static final String TEST_OFFENCE_ID = "324234";

    public static final Integer PASSPORT_ID = 23456;

    public static final LocalDateTime TEST_CROWN_REP_ORDER_DATE =
            LocalDateTime.of(2022, 10, 19, 0, 0, 0);

    public static final LocalDateTime INCOME_EVIDENCE_DATE = LocalDateTime.of(2023, 6, 5, 15, 0, 0);
    public static final LocalDateTime CAPITAL_EVIDENCE_DATE = LocalDateTime.of(2023, 6, 5, 15, 0, 0);

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

    public static ApiFinancialAssessment getFinancialAssessment() {
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
                .withRepOrderDecision("Granted - Passed Means Test")
                .withRepOrderDate(TEST_DECISION_DATE)
                .withRepType("Declined Rep Order");
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
                .applicantHistoryId(TEST_APPLICANT_HISTORY_ID)
                .incomeEvidenceReceivedDate(INCOME_EVIDENCE_DATE)
                .capitalEvidenceReceivedDate(CAPITAL_EVIDENCE_DATE)
                .evidenceFeeLevel(EvidenceFeeLevel.LEVEL1)
                .capitalEvidence(List.of(getCapitalEvidenceDTO(TEST_DATE_RECEIVED, "Type")))
                .build();
    }

    public static ApiCrownCourtSummary getCrownCourtSummary() {
        return new ApiCrownCourtSummary()
                .withRepOrderDecision(MOCK_DECISION)
                .withRepOrderDate(TEST_REP_ORDER_DATE)
                .withRepId(TEST_REP_ID)
                .withIsImprisoned(true)
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
                .withFinancialAssessment(getFinancialAssessment())
                .withPassportAssessment(getPassportAssessment());
    }

    public static RepOrderCCOutcomeDTO getRepOrderCCOutcomeDTO(Integer outcomeId, String outcome, LocalDateTime outcomeDate) {
        return RepOrderCCOutcomeDTO.builder()
                .id(outcomeId)
                .outcome(outcome)
                .outcomeDate(outcomeDate)
                .build();
    }

    public static OffenceSummary getOffenceSummary(UUID uuid, String changeDate) {
        return OffenceSummary.builder()
                .offenceId(uuid)
                .proceedingsConcludedChangedDate(changeDate)
                .build();
    }

    public static Verdict getVerdict(String verdictType, String verdictDate) {
        return Verdict.builder()
                .verdictType(VerdictType.builder().categoryType(verdictType).build())
                .verdictDate(verdictDate)
                .build();
    }

    public static ProsecutionConcluded getProsecutionConcluded() {
        return ProsecutionConcluded.builder()
                .isConcluded(true)
                .maatId(123456)
                .offenceSummary(List.of(
                        OffenceSummary.builder()
                                .offenceCode("1212")
                                .verdict(getVerdict("GUILTY", "2021-11-12"))
                                .plea(Plea.builder().value("NOT_GUILTY").pleaDate("2021-11-12").build())
                                .proceedingsConcludedChangedDate("2021-11-12")
                                .build()
                ))
                .build();
    }

    public static WQHearingDTO getWQHearingDTO() {

        return WQHearingDTO.builder()
                .hearingUUID(UUID.randomUUID().toString())
                .ouCourtLocation("loc1")
                .wqJurisdictionType("Type")
                .caseUrn(TEST_CASE_ID.toString())
                .resultCodes("code1,code2,code3")
                .build();

    }

    public static ProsecutionConcludedEntity getProsecutionConcludedEntity() {
        return ProsecutionConcludedEntity
                .builder()
                .maatId(TEST_REP_ID)
                .caseData(getCaseData().getBytes(StandardCharsets.UTF_8))
                .build();
    }

    public static String getCaseData() {

        return """
                {
                   "maatId":5636361,
                   "defendantId":"9c26435c-b262-4318-9927-f40bc4e7f0c7",
                   "prosecutionCaseId":"1d329c30-936c-11ec-b909-0242ac120002",
                   "isConcluded":true,
                   "hearingIdWhereChangeOccurred":"0ffd1c68-9428-11ec-b909-0242ac120002",
                   "offenceSummary":[
                      {
                         "offenceId":"40989abc-2d8c-431a-8692-535848b2e918",
                         "offenceCode":"PT00011",
                         "proceedingsConcluded":false,
                         "plea":{
                            "originatingHearingId":"08c98420-5f6a-4839-b48b-19646e81619a",
                            "value":"NOT_GUILTY",
                            "pleaDate":"2022-02-10"
                         },
                         "verdict":{
                            "verdictDate":"2022-02-10",
                            "originatingHearingId":"08c98420-5f6a-4839-b48b-19646e81619a",
                            "verdictType":{
                               "verdictTypeId":"dfd71ee7-039d-3d93-ae37-98ef38aec6e4",
                               "sequence":10,
                               "description":"Found guilty",
                               "category":"Guilty",
                               "categoryType":"GUILTY_BY_JURY_CONVICTED"
                            }
                         },
                         "proceedingsConcludedChangedDate":"2022-02-10"
                      },
                      {
                         "offenceId":"b72f793a-93ed-11ec-b909-0242ac120002",
                         "offenceCode":"PT00011",
                         "proceedingsConcluded":false,
                         "plea":{
                            "originatingHearingId":"08c98420-5f6a-4839-b48b-19646e81619a",
                            "value":"NOT_GUILTY",
                            "pleaDate":"2022-02-10"
                         },
                         "proceedingsConcludedChangedDate":"2022-02-10"
                      }
                   ],
                   "messageRetryCounter":0,
                   "retryCounterForHearing":0,
                   "metadata":{
                      "laaTransactionId":"ea0af85c36b17113389bb9aae924e9ad"
                   }
                }
                          """;
    }

    public static FinancialAssessmentDTO getFinancialAssessmentDTO() {
        return FinancialAssessmentDTO.builder()
                .id(1)
                .assessmentType("Full")
                .build();
    }

    public static ApiUpdateApplicationResponse getApiUpdateApplicationResponse() {
        return new ApiUpdateApplicationResponse()
                .withModifiedDateTime(TEST_DATE_MODIFIED)
                .withCrownRepOrderDate(TEST_CROWN_REP_ORDER_DATE)
                .withCrownRepOrderDecision(Constants.GRANTED_PASSED_MEANS_TEST)
                .withCrownRepOrderType(Constants.CROWN_COURT_ONLY);
    }

    public static RepOrderDTO getRepOrderDTO() {
        return RepOrderDTO.builder()
                .dateModified(TEST_DATE_MODIFIED)
                .crownRepOrderDecision(Constants.GRANTED_PASSED_MEANS_TEST)
                .crownRepOrderDate(TEST_CROWN_REP_ORDER_DATE.toLocalDate())
                .crownRepOrderType(Constants.CROWN_COURT_ONLY)
                .evidenceFeeLevel(EvidenceFeeLevel.LEVEL1.getFeeLevel())
                .build();
    }

    public static ApiCalculateEvidenceFeeResponse getApiCalculateEvidenceFeeResponse() {
        ApiCalculateEvidenceFeeResponse response = new ApiCalculateEvidenceFeeResponse();
        response.setEvidenceFee(new ApiEvidenceFee().withFeeLevel(EvidenceFeeLevel.LEVEL1.getFeeLevel())
                .withDescription(EvidenceFeeLevel.LEVEL1.getDescription()));
        return response;
    }

    public static ApiCapitalEvidence getCapitalEvidenceDTO(LocalDateTime dataReceived, String evidenceType) {
        ApiCapitalEvidence evidence = new ApiCapitalEvidence();
        evidence.setDateReceived(dataReceived);
        evidence.setEvidenceType(evidenceType);
        return evidence;

    }

    public static ApiCalculateEvidenceFeeRequest getApiCalculateEvidenceFeeRequest() {
        ApiCalculateEvidenceFeeRequest request = new ApiCalculateEvidenceFeeRequest();
        request.setRepId(TEST_REP_ID);
        request.setMagCourtOutcome(MagCourtOutcome.COMMITTED_FOR_TRIAL.getOutcome());
        ApiEvidenceFee evidenceFee = new ApiEvidenceFee();
        evidenceFee.setFeeLevel(EvidenceFeeLevel.LEVEL1.getFeeLevel());
        evidenceFee.setDescription(EvidenceFeeLevel.LEVEL1.getDescription());
        request.setEvidenceFee(evidenceFee);
        request.setCapitalEvidenceReceivedDate(CAPITAL_EVIDENCE_DATE);
        request.setIncomeEvidenceReceivedDate(INCOME_EVIDENCE_DATE);
        return request;
    }

    public static ApiUpdateCrownCourtOutcomeResponse getApiUpdateCrownCourtOutcomeResponse() {
        ApiUpdateCrownCourtOutcomeResponse response = new ApiUpdateCrownCourtOutcomeResponse();
        response.setModifiedDateTime(TEST_DATE_MODIFIED);
        ApiCrownCourtSummary summary = new ApiCrownCourtSummary();
        summary.setEvidenceFeeLevel(EvidenceFeeLevel.LEVEL1.getFeeLevel());
        summary.setRepOrderDate(TEST_REP_ORDER_DATE);
        summary.setRepOrderDecision("");
        summary.setRepType("");
        ApiRepOrderCrownCourtOutcome outcome = new ApiRepOrderCrownCourtOutcome();
        outcome.setOutcome(CrownCourtOutcome.CONVICTED);
        outcome.setOutcomeDate(TEST_CROWN_REP_ORDER_DATE);
        response.setCrownCourtSummary(summary);
        return response;
    }
}