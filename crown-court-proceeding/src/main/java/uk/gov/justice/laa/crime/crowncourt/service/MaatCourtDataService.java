package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.util.GraphqlSchemaReaderUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaatCourtDataService {

    @Qualifier("maatApiClient")
    private final RestAPIClient maatAPIClient;
    private final ServicesConfiguration configuration;
    public static final String RESPONSE_STRING = "Response from Court Data API: {}";

    public IOJAppealDTO getCurrentPassedIOJAppealFromRepId(Integer repId, String laaTransactionId) {
        IOJAppealDTO response = maatAPIClient.get(
                new ParameterizedTypeReference<IOJAppealDTO>() {
                },
                configuration.getMaatApi().getIojAppealEndpoints().getFindUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId),
                repId
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public RepOrderDTO updateRepOrder(UpdateRepOrderRequestDTO updateRepOrderRequestDTO, String laaTransactionId) {
        RepOrderDTO response = maatAPIClient.put(
                updateRepOrderRequestDTO,
                new ParameterizedTypeReference<RepOrderDTO>() {
                },
                configuration.getMaatApi().getRepOrderEndpoints().getUpdateUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId)
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<RepOrderCCOutcomeDTO> getRepOrderCCOutcomeByRepId(Integer repId, String laaTransactionId) {
        List<RepOrderCCOutcomeDTO> response = maatAPIClient.get(
                new ParameterizedTypeReference<List<RepOrderCCOutcomeDTO>>() {
                },
                configuration.getMaatApi().getRepOrderEndpoints().getFindOutcomeUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId),
                repId
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public RepOrderCCOutcomeDTO createOutcome(RepOrderCCOutcomeDTO outcomeDTO, String laaTransactionId) {
        RepOrderCCOutcomeDTO response = maatAPIClient.post(
                outcomeDTO,
                new ParameterizedTypeReference<RepOrderCCOutcomeDTO>() {
                },
                configuration.getMaatApi().getRepOrderEndpoints().getCreateOutcomeUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId));
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public long outcomeCount(Integer repId, String laaTransactionId) {

        ResponseEntity<Void> response = maatAPIClient.head(
                configuration.getMaatApi().getRepOrderEndpoints().getFindOutcomeUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId),
                repId
        );
        log.info(RESPONSE_STRING, response);
        return response.getHeaders().getContentLength();
    }

    private static Map<String, Object> getGraphQLRequestBody(Integer repId, String sentenceOrdDate) throws IOException {
        final String query = GraphqlSchemaReaderUtil.getSchemaFromFileName("repOrderFilter");

        Map<String, Object> variablesMap = new HashMap<>();
        variablesMap.put("repId", repId);
        variablesMap.put("sentenceOrdDate", sentenceOrdDate);

        Map<String, Object> graphQLBody = new HashMap<>();
        graphQLBody.put("query", query);
        graphQLBody.put("variables", variablesMap);
        return graphQLBody;
    }
    public Object getRepOrderByFilter(Integer repId, String sentenceOrdDate) throws IOException {
        Map<String, Object> graphQLBody = getGraphQLRequestBody(repId, sentenceOrdDate);
        Object response = maatAPIClient.getGraphQLApiResponse(
                Object.class,
                configuration.getGraphQLEndpoints().getGraphqlQueryUrl(),
                graphQLBody
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }


}
