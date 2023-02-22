package uk.gov.justice.laa.crime.crowncourt.config;

public class MockServicesConfiguration {

    public static ServicesConfiguration getConfiguration(int port) {

        String host = String.format("http://localhost:%s", port);

        ServicesConfiguration servicesConfiguration = new ServicesConfiguration();
        ServicesConfiguration.MaatApi maatApiConfiguration = new ServicesConfiguration.MaatApi();
        ServicesConfiguration.CourtDataAdapter courtDataAdapterConfig = new ServicesConfiguration.CourtDataAdapter();

        ServicesConfiguration.MaatApi.IOJAppealEndpoints iojEndpoints =
                new ServicesConfiguration.MaatApi.IOJAppealEndpoints("/ioj-appeal/{repId}");

        ServicesConfiguration.MaatApi.HardshipReviewEndpoints hardshipEndpoints =
                new ServicesConfiguration.MaatApi.HardshipReviewEndpoints("/hardship/{repId}");

        ServicesConfiguration.MaatApi.PassportAssessmentEndpoints passportEndpoints =
                new ServicesConfiguration.MaatApi.PassportAssessmentEndpoints("/passport-assessments/{repId}");

        ServicesConfiguration.MaatApi.RepOrderEndpoints repOrderEndpoints =
                new ServicesConfiguration.MaatApi.RepOrderEndpoints(
                        "/rep-orders/{repId}",
                        "/rep-orders",
                        "rep-orders/cc-outcome",
                        "/rep-orders/cc-outcome/reporder/{repId}"
                );

        ServicesConfiguration.MaatApi.ValidationEndpoints validationEndpoints =
                new ServicesConfiguration.MaatApi.ValidationEndpoints(
                        "/authorization/users/{username}/actions/{action}",
                        "/authorization/users/{username}/work-reasons/{nworCode}",
                        "/authorization/users/{username}/reservations/{reservationId}/sessions/{sessionId}",
                        "/financial-assessments/check-outstanding/{repId}"
                );

        maatApiConfiguration.setBaseUrl(host);
        maatApiConfiguration.setOAuthEnabled(false);
        maatApiConfiguration.setIojAppealEndpoints(iojEndpoints);
        maatApiConfiguration.setHardshipReviewEndpoints(hardshipEndpoints);
        maatApiConfiguration.setPassportAssessmentEndpoints(passportEndpoints);
        maatApiConfiguration.setRepOrderEndpoints(repOrderEndpoints);
        maatApiConfiguration.setValidationEndpoints(validationEndpoints);

        courtDataAdapterConfig.setBaseUrl(host);
        courtDataAdapterConfig.setHearingUrl("/api/internal/v2/hearing_results/{hearingId}");

        servicesConfiguration.setMaatApi(maatApiConfiguration);
        servicesConfiguration.setCourtDataAdapter(courtDataAdapterConfig);

        return servicesConfiguration;
    }
}
