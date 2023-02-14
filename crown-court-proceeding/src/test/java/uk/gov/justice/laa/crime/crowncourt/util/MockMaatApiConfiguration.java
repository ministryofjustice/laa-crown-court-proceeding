package uk.gov.justice.laa.crime.crowncourt.util;

import uk.gov.justice.laa.crime.crowncourt.config.MaatApiConfiguration;

public class MockMaatApiConfiguration {

    public static MaatApiConfiguration getConfiguration(int port) {

        MaatApiConfiguration configuration = new MaatApiConfiguration();

        MaatApiConfiguration.IOJAppealEndpoints iojEndpoints =
                new MaatApiConfiguration.IOJAppealEndpoints("/ioj-appeal/{repId}");
        MaatApiConfiguration.HardshipReviewEndpoints hardshipEndpoints =
                new MaatApiConfiguration.HardshipReviewEndpoints("/hardship/{repId}");
        MaatApiConfiguration.PassportAssessmentEndpoints passportEndpoints =
                new MaatApiConfiguration.PassportAssessmentEndpoints("/passport-assessments/{repId}");
        MaatApiConfiguration.RepOrderEndpoints repOrderEndpoints =
                new MaatApiConfiguration.RepOrderEndpoints(
                        "/rep-orders/{repId}",
                        "/rep-orders",
                        "rep-orders/cc-outcome",
                        "/rep-orders/cc-outcome/reporder/{repId}"
                );
        MaatApiConfiguration.ValidationEndpoints validationEndpoints = new MaatApiConfiguration.ValidationEndpoints(
                "/authorization/users/{username}/actions/{action}",
                "/authorization/users/{username}/work-reasons/{nworCode}",
                "/authorization/users/{username}/reservations/{reservationId}/sessions/{sessionId}",
                "/financial-assessments/check-outstanding/{repId}"
        );
        configuration.setBaseUrl(
                String.format("http://localhost:%s", port)
        );
        configuration.setOAuthEnabled(false);
        configuration.setIojAppealEndpoints(iojEndpoints);
        configuration.setHardshipReviewEndpoints(hardshipEndpoints);
        configuration.setPassportAssessmentEndpoints(passportEndpoints);
        configuration.setRepOrderEndpoints(repOrderEndpoints);
        configuration.setValidationEndpoints(validationEndpoints);

        return configuration;
    }
}
