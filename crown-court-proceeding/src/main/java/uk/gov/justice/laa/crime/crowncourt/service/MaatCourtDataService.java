package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.client.MaatCourtDataClient;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.config.MaatApiConfiguration;
import uk.gov.justice.laa.crime.crowncourt.dto.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.util.GraphqlSchemaReaderUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaatCourtDataService {

    public static final String RESPONSE_STRING = "Response from Court Data API: %s";
    private final MaatApiConfiguration configuration;
    private final MaatCourtDataClient maatCourtDataClient;

    private static Map<String, Object> getGraphQLRequestBody(String repId, String sentenceOrdDate) throws IOException {
        final String query = GraphqlSchemaReaderUtil.getSchemaFromFileName("repOrderFilter");

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("repId", repId);
        variablesMap.put("sentenceOrdDate", sentenceOrdDate);

        Map<String, Object> graphQLBody = new HashMap<>();
        graphQLBody.put("query", query);
        graphQLBody.put("variables", variablesMap);
        return graphQLBody;
    }

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

    public void updateRepOrder(UpdateRepOrderRequestDTO updateRepOrderRequestDTO, String laaTransactionId) {
        maatCourtDataClient.getApiResponseViaPUT(
                updateRepOrderRequestDTO,
                Void.class,
                configuration.getRepOrderEndpoints().getUpdateUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId)
        );
    }

    public Object getRepOrderByFilter(String repId, String sentenceOrdDate) throws IOException {
        Map<String, Object> graphQLBody = getGraphQLRequestBody(repId, sentenceOrdDate);
        Object response = maatCourtDataClient.getGraphQLApiResponse(
                Object.class,
                configuration.getGraphQLEndpoints().getGraphqlQueryUrl(),
                graphQLBody
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }

    public List<RepOrderCCOutcomeDTO> getRepOrderCCOutcomeByRepId(Integer repId, String laaTransactionId) {
        List<RepOrderCCOutcomeDTO> response = maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getRepOrderEndpoints().getFindOutcomeUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId),
                repId
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }


}
