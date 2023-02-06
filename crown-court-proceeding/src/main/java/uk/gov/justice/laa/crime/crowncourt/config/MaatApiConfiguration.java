package uk.gov.justice.laa.crime.crowncourt.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "maat-api")
public class MaatApiConfiguration {
    /**
     * The API's Base URL
     */
    @NotNull
    private String baseUrl;

    /**
     * Determines whether oAuth authentication is enabled
     */
    @NotNull
    private boolean oAuthEnabled;

    /**
     * Defines validation endpoint URLs
     */
    @NotNull
    private ValidationEndpoints validationEndpoints;

    @NotNull
    private RepOrderEndpoints repOrderEndpoints;

    @NotNull
    private PassportAssessmentEndpoints passportAssessmentEndpoints;

    @NotNull
    private HardshipReviewEndpoints hardshipReviewEndpoints;

    @NotNull
    private IOJAppealEndpoints iojAppealEndpoints;

    @NotNull
    private GraphQLEndpoints graphQLEndpoints;

    @NotNull
    private OffenceEndpoints offenceEndpoints;

    @NotNull
    private WqHearingEndpoints wqHearingEndpoints;

    @NotNull
    private WqOffenceEndpoints wqOffenceEndpoints;

    @NotNull
    private WqLinkRegisterEndpoints wqLinkRegisterEndpoints;

    @NotNull
    private XlatResultEndpoints xlatResultEndpoints;

    @NotNull
    private ResultEndpoints resultEndpoints;

    @NotNull
    private WqResultEndpoints WqResultEndpoints;

    @NotNull
    private CrownCourtStoredProcedureEndpoints crownCourtStoredProcedureEndpoints;

    @Getter
    @AllArgsConstructor
    @Setter
    @NoArgsConstructor
    public static class ValidationEndpoints {
        /**
         * Validate Role Action Endpoint URL
         */
        @NotNull
        private String roleActionUrl;

        /**
         * Validate New Work Reason Endpoint URL
         */
        @NotNull
        private String newWorkReasonUrl;

        /**
         * Validate Reservation Endpoint URL
         */
        @NotNull
        private String reservationsUrl;

        /**
         * Check Outstanding Assessments Endpoint URL
         */
        @NotNull
        private String outstandingAssessmentsUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RepOrderEndpoints {

        @NotNull
        private String findUrl;

        @NotNull
        private String updateUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PassportAssessmentEndpoints {

        @NotNull
        private String findUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HardshipReviewEndpoints {

        @NotNull
        private String findUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IOJAppealEndpoints {

        @NotNull
        private String findUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GraphQLEndpoints {

        @NotNull
        private String graphqlQueryUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OffenceEndpoints {

        @NotNull
        private String findUrl;
        @NotNull
        private String offenceCountUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WqHearingEndpoints {

        @NotNull
        private String findUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WqLinkRegisterEndpoints {

        @NotNull
        private String findUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WqOffenceEndpoints {

        @NotNull
        private String wqOffenceCountUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class XlatResultEndpoints {

        @NotNull
        private String resultCodesForCCImprisonmentUrl;
        @NotNull
        private String resultCodesForCCBenchWarrantUrl;
        @NotNull
        private String resultCodesForWQTypeSubTypeUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResultEndpoints {
        @NotNull
        private String resultCodeByCaseIdAndAsnSeqUrl;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WqResultEndpoints {
        @NotNull
        private String resultCodeByCaseIdAndAsnSeqUrl;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CrownCourtStoredProcedureEndpoints {
        @NotNull
        private String updateCrownCourtOutcomeUrl;
    }

}
