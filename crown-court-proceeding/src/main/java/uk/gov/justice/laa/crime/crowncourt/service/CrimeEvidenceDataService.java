package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.client.CrimeEvidenceClient;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCalculateEvidenceFeeRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCalculateEvidenceFeeResponse;
import uk.gov.justice.laa.crime.crowncourt.model.ApiFinancialAssessment;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrimeEvidenceDataService {

    private final CrimeEvidenceClient evidenceAPIClient;
    private final ServicesConfiguration configuration;
    public static final String RESPONSE_STRING = "Response from Court Data API: %s";

    public ApiCalculateEvidenceFeeResponse getCalEvidenceFee(ApiCalculateEvidenceFeeRequest evidenceFeeRequest) {
        ApiCalculateEvidenceFeeResponse response = evidenceAPIClient.getApiResponseViaPOST(
                evidenceFeeRequest,
                ApiCalculateEvidenceFeeResponse.class,
                configuration.getEvidence().getEvidenceFeeEndpoints().getEvidenceFeeUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, evidenceFeeRequest.getLaaTransactionId())
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }
}
