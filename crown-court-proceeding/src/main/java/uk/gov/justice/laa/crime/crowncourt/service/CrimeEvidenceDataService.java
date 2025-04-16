package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiCalculateEvidenceFeeRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiCalculateEvidenceFeeResponse;
import uk.gov.justice.laa.crime.crowncourt.client.EvidenceApiClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrimeEvidenceDataService {
    
    private final EvidenceApiClient evidenceAPIClient;
    private static final String RESPONSE_STRING = "Response from the Evidence service: {}";

    public ApiCalculateEvidenceFeeResponse getCalculateEvidenceFee(ApiCalculateEvidenceFeeRequest evidenceFeeRequest) {
        log.debug("Request to calculate evidence fee: {}", evidenceFeeRequest);
        ApiCalculateEvidenceFeeResponse response = evidenceAPIClient.calculateEvidenceFee(evidenceFeeRequest);
        log.debug(RESPONSE_STRING, response);
        return response;
    }
}
