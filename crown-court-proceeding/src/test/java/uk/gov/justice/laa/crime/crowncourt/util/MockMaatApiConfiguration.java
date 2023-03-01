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
        MaatApiConfiguration.WqHearingEndpoints wqHearingEndpoints =
                new MaatApiConfiguration.WqHearingEndpoints(
                        "wq-hearing/{hearingUUID}/maatId/{maatId}");

        MaatApiConfiguration.WqLinkRegisterEndpoints wqLinkRegisterEndpoints =
                new MaatApiConfiguration.WqLinkRegisterEndpoints("wq-link-register/{maatId}");
        configuration.setBaseUrl(
                String.format("http://localhost:%s", port)
        );
        MaatApiConfiguration.OffenceEndpoints offenceEndpoints =
                new MaatApiConfiguration.OffenceEndpoints("/offence/case/{caseId}",
                        "/offence/{offenceId}/case/{caseId}");

        MaatApiConfiguration.WqOffenceEndpoints wqOffenceEndpoints =
                new MaatApiConfiguration.WqOffenceEndpoints("/wq-offence/{offenceId}/case/{caseId}");

        MaatApiConfiguration.XlatResultEndpoints xlatResultEndpoints =
                new MaatApiConfiguration.XlatResultEndpoints(
                        "/xlat-result/cc-imprisonment" ,
                        "xlat-result/cc-bench-warrant",
                        "/xlat-result/wqType/{wqType}/subType/{subType}");

        MaatApiConfiguration.CrownCourtStoredProcedureEndpoints storedProcedureEndpoints =
                new MaatApiConfiguration.CrownCourtStoredProcedureEndpoints("/crown-court/updateCCOutcome");


        MaatApiConfiguration.CrownCourtProcessingEndpoints processingEndpoints =
                new MaatApiConfiguration.CrownCourtProcessingEndpoints(
                        "/crown-court/update-appeal-cc-sentence",
                        "/crown-court/update-cc-sentence");

        MaatApiConfiguration.ReservationsEndpoints reservationsEndpoints =
                new MaatApiConfiguration.ReservationsEndpoints("/reservations/{maatId}");

        MaatApiConfiguration.GraphQLEndpoints graphQLEndpoints =
                new MaatApiConfiguration.GraphQLEndpoints("/graphQL");

        MaatApiConfiguration.ResultEndpoints resultEndpoints =
                new MaatApiConfiguration.ResultEndpoints("/result/caseId/{caseId}/asnSeq/{asnSeq}");

        MaatApiConfiguration.WqResultEndpoints wqResultEndpoints =
                new MaatApiConfiguration.WqResultEndpoints("/wq-result/caseId/{caseId}/asnSeq/{asnSeq}");


        configuration.setOAuthEnabled(false);
        configuration.setIojAppealEndpoints(iojEndpoints);
        configuration.setHardshipReviewEndpoints(hardshipEndpoints);
        configuration.setPassportAssessmentEndpoints(passportEndpoints);
        configuration.setRepOrderEndpoints(repOrderEndpoints);
        configuration.setValidationEndpoints(validationEndpoints);
        configuration.setWqHearingEndpoints(wqHearingEndpoints);
        configuration.setWqLinkRegisterEndpoints(wqLinkRegisterEndpoints);
        configuration.setOffenceEndpoints(offenceEndpoints);
        configuration.setWqOffenceEndpoints(wqOffenceEndpoints);
        configuration.setXlatResultEndpoints(xlatResultEndpoints);
        configuration.setCrownCourtStoredProcedureEndpoints(storedProcedureEndpoints);
        configuration.setCrownCourtProcessingEndpoints(processingEndpoints);
        configuration.setReservationsEndpoints(reservationsEndpoints);
        configuration.setGraphQLEndpoints(graphQLEndpoints);
        configuration.setResultEndpoints(resultEndpoints);
        configuration.setWqResultEndpoints(wqResultEndpoints);

        return configuration;
    }
}
