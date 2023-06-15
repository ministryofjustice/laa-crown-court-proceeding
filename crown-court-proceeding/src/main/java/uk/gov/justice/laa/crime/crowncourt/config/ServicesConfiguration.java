package uk.gov.justice.laa.crime.crowncourt.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;

@Data
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "services")
public class ServicesConfiguration {

    @NotNull
    private MaatApi maatApi;

    @NotNull
    private CourtDataAdapter courtDataAdapter;

    @NotNull
    private Evidence  evidence;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaatApi {
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
        private WqResultEndpoints wqResultEndpoints;

        @NotNull
        private CrownCourtStoredProcedureEndpoints crownCourtStoredProcedureEndpoints;

        @NotNull
        private CrownCourtProcessingEndpoints crownCourtProcessingEndpoints;

        @NotNull
        private ReservationEndpoints reservationEndpoints;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
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

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RepOrderEndpoints {

            @NotNull
            private String findUrl;

            @NotNull
            private String updateUrl;

            @NotNull
            private String findOutcomeUrl;

            @NotNull
            private String createOutcomeUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PassportAssessmentEndpoints {

            @NotNull
            private String findUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class HardshipReviewEndpoints {

            @NotNull
            private String findUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class IOJAppealEndpoints {

            @NotNull
            private String findUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OffenceEndpoints {

            @NotNull
            private String findUrl;
            @NotNull
            private String offenceCountUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WqHearingEndpoints {

            @NotNull
            private String findUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WqLinkRegisterEndpoints {

            @NotNull
            private String findUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WqOffenceEndpoints {

            @NotNull
            private String wqOffenceCountUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class XlatResultEndpoints {

            @NotNull
            private String resultCodesForCCImprisonmentUrl;
            @NotNull
            private String resultCodesForCCBenchWarrantUrl;
            @NotNull
            private String resultCodesForWQTypeSubTypeUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ResultEndpoints {
            @NotNull
            private String resultCodeByCaseIdAndAsnSeqUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WqResultEndpoints {
            @NotNull
            private String resultCodeByCaseIdAndAsnSeqUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CrownCourtStoredProcedureEndpoints {
            @NotNull
            private String updateCrownCourtOutcomeUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class CrownCourtProcessingEndpoints {
            @NotNull
            private String updateAppealCcSentenceUrl;
            @NotNull
            private String updateCcSentenceUrl;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ReservationEndpoints {
            @NotNull
            private String isMaatRecordLockedUrl;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourtDataAdapter {

        @NotNull
        private String baseUrl;

        @NotNull
        private String hearingUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Evidence {

        @NotNull
        private String baseUrl;

        @NotNull
        private EvidenceFeeEndpoints evidenceFeeEndpoints;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EvidenceFeeEndpoints {
            @NotNull
            private String evidenceFeeUrl;
        }
    }
}
