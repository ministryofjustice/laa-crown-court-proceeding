package uk.gov.justice.laa.crime.crowncourt.common;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

    // Crown Court Rep Decisions
    public static final String GRANTED_FAILED_MEANS_TEST = "Granted - Failed Means Test";
    public static final String FAILED_CF_S_FAILED_MEANS_TEST = "Failed - CfS Failed Means Test";
    public static final String GRANTED_PASSED_MEANS_TEST = "Granted - Passed Means Test";
    public static final String REFUSED_INELIGIBLE = "Refused - Ineligible";
    public static final String GRANTED_PASSPORTED = "Granted - Passported";
    public static final String FAILED_IO_J_APPEAL_FAILURE = "Failed - IoJ Appeal Failure";

    // Crown Court Rep Types
    public static final String CROWN_COURT_ONLY = "Crown Court Only";
    public static final String DECLINED_REP_ORDER = "Declined Rep Order";
    public static final String NOT_ELIGIBLE_FOR_REP_ORDER = "Not eligible for Rep Order";
    public static final String THROUGH_ORDER = "Through Order";

    public static final String LAA_TRANSACTION_ID = "Laa-Transaction-Id";

    public static final Integer COMMITTAL_FOR_TRIAL_SUB_TYPE = 2;
    public static final Integer COMMITTAL_FOR_SENTENCE_SUB_TYPE = 1;
    public static final String YES = "Y";
    public static final String NO = "N";

    // Error messages
    public static final String MISSING_REGISTRATION_ID = "registrationId cannot be null";
}
