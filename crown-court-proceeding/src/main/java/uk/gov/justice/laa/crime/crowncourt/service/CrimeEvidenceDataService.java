package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.model.request.ApiCalculateEvidenceFeeRequest;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiCalculateEvidenceFeeResponse;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrimeEvidenceDataService {

    @Qualifier("evidenceApiClient")
    private final RestAPIClient evidenceAPIClient;
    private final ServicesConfiguration configuration;
    private static final String RESPONSE_STRING = "Response from the Evidence service: {}";

    public ApiCalculateEvidenceFeeResponse getCalEvidenceFee(ApiCalculateEvidenceFeeRequest evidenceFeeRequest) {
        ApiCalculateEvidenceFeeResponse response = evidenceAPIClient.post(
                evidenceFeeRequest,
                new ParameterizedTypeReference<ApiCalculateEvidenceFeeResponse>() {},
                configuration.getEvidence().getEvidenceFeeEndpoints().getEvidenceFeeUrl(),
                Map.of()
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }
}
