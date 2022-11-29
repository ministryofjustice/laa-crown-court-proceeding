package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.client.MaatCourtDataClient;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.config.MaatApiConfiguration;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaatCourtDataService {

    private final MaatApiConfiguration configuration;
    private final MaatCourtDataClient maatCourtDataClient;
    public static final String RESPONSE_STRING = "Response from Court Data API: %s";

    public IOJAppealDTO getCurrentPassedIOJAppealFromRepId(Integer repId, String laaTransactionId) {
        IOJAppealDTO response = maatCourtDataClient.getApiResponseViaGET(
                IOJAppealDTO.class,
                configuration.getIojAppealEndpoints().getFindUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId),
                repId
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }

}
