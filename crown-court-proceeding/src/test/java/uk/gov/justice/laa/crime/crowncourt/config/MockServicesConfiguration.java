package uk.gov.justice.laa.crime.crowncourt.config;

public class MockServicesConfiguration {

    public static ServicesConfiguration getConfiguration(int port) {

        String host = String.format("http://localhost:%s", port);

        ServicesConfiguration servicesConfiguration = new ServicesConfiguration();
        ServicesConfiguration.MaatApi maatApiConfiguration = new ServicesConfiguration.MaatApi();
        ServicesConfiguration.CourtDataAdapter courtDataAdapterConfig = new ServicesConfiguration.CourtDataAdapter();
        ServicesConfiguration.Evidence evidenceConfig = new ServicesConfiguration.Evidence();

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

        ServicesConfiguration.MaatApi.WqHearingEndpoints wqHearingEndpoints =
                new ServicesConfiguration.MaatApi.WqHearingEndpoints(
                        "wq-hearing/{hearingUUID}/maatId/{maatId}");

        ServicesConfiguration.MaatApi.WqLinkRegisterEndpoints wqLinkRegisterEndpoints =
                new ServicesConfiguration.MaatApi.WqLinkRegisterEndpoints("wq-link-register/{maatId}");

        ServicesConfiguration.MaatApi.OffenceEndpoints offenceEndpoints =
                new ServicesConfiguration.MaatApi.OffenceEndpoints("/offence/case/{caseId}",
                        "/offence/{offenceId}/case/{caseId}");

        ServicesConfiguration.MaatApi.WqOffenceEndpoints wqOffenceEndpoints =
                new ServicesConfiguration.MaatApi.WqOffenceEndpoints("/wq-offence/{offenceId}/case/{caseId}");

        ServicesConfiguration.MaatApi.XlatResultEndpoints xlatResultEndpoints =
                new ServicesConfiguration.MaatApi.XlatResultEndpoints(
                        "/xlat-result/cc-imprisonment" ,
                        "xlat-result/cc-bench-warrant",
                        "/xlat-result/wqType/{wqType}/subType/{subType}");

        ServicesConfiguration.MaatApi.CrownCourtStoredProcedureEndpoints storedProcedureEndpoints =
                new ServicesConfiguration.MaatApi.CrownCourtStoredProcedureEndpoints("/crown-court/updateCCOutcome");


        ServicesConfiguration.MaatApi.CrownCourtProcessingEndpoints processingEndpoints =
                new ServicesConfiguration.MaatApi.CrownCourtProcessingEndpoints(
                        "/crown-court/update-appeal-cc-sentence",
                        "/crown-court/update-cc-sentence");

        ServicesConfiguration.MaatApi.ReservationEndpoints reservationsEndpoints =
                new ServicesConfiguration.MaatApi.ReservationEndpoints("/reservations/{maatId}");

        ServicesConfiguration.MaatApi.ResultEndpoints resultEndpoints =
                new ServicesConfiguration.MaatApi.ResultEndpoints("/result/caseId/{caseId}/asnSeq/{asnSeq}");

        ServicesConfiguration.MaatApi.WqResultEndpoints wqResultEndpoints =
                new ServicesConfiguration.MaatApi.WqResultEndpoints("/wq-result/caseId/{caseId}/asnSeq/{asnSeq}");

        ServicesConfiguration.Evidence.EvidenceFeeEndpoints  evidenceFeeEndpoints =
                new ServicesConfiguration.Evidence.EvidenceFeeEndpoints("api/internal/v1/evidence/calculate-evidence-fee");

        maatApiConfiguration.setBaseUrl(host);
        maatApiConfiguration.setOAuthEnabled(false);
        maatApiConfiguration.setIojAppealEndpoints(iojEndpoints);
        maatApiConfiguration.setHardshipReviewEndpoints(hardshipEndpoints);
        maatApiConfiguration.setPassportAssessmentEndpoints(passportEndpoints);
        maatApiConfiguration.setRepOrderEndpoints(repOrderEndpoints);
        maatApiConfiguration.setValidationEndpoints(validationEndpoints);
        maatApiConfiguration.setWqHearingEndpoints(wqHearingEndpoints);
        maatApiConfiguration.setWqLinkRegisterEndpoints(wqLinkRegisterEndpoints);
        maatApiConfiguration.setOffenceEndpoints(offenceEndpoints);
        maatApiConfiguration.setWqOffenceEndpoints(wqOffenceEndpoints);
        maatApiConfiguration.setXlatResultEndpoints(xlatResultEndpoints);
        maatApiConfiguration.setCrownCourtStoredProcedureEndpoints(storedProcedureEndpoints);
        maatApiConfiguration.setCrownCourtProcessingEndpoints(processingEndpoints);
        maatApiConfiguration.setReservationEndpoints(reservationsEndpoints);
        maatApiConfiguration.setResultEndpoints(resultEndpoints);
        maatApiConfiguration.setWqResultEndpoints(wqResultEndpoints);

        courtDataAdapterConfig.setBaseUrl(host);
        courtDataAdapterConfig.setHearingUrl("/api/internal/v2/hearing_results/{hearingId}");

        servicesConfiguration.setMaatApi(maatApiConfiguration);
        servicesConfiguration.setCourtDataAdapter(courtDataAdapterConfig);

        servicesConfiguration.setEvidence(evidenceConfig);
        evidenceConfig.setEvidenceFeeEndpoints(evidenceFeeEndpoints);
        evidenceConfig.setBaseUrl(host);

        return servicesConfiguration;
    }
}
