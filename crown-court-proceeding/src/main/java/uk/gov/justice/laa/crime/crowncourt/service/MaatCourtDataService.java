package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.client.MaatCourtDataClient;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.config.MaatApiConfiguration;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.*;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateSentenceOrder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.util.GraphqlSchemaReaderUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

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

    public WQHearingDTO retrieveHearingForCaseConclusion(ProsecutionConcluded prosecutionConcluded) {

        WQHearingDTO wqHearingDTO = null;
        List<WQHearingDTO> wqHearingList = maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getWqHearingEndpoints().getFindUrl(),
                emptyMap(),
                prosecutionConcluded
        );
        if (wqHearingList != null && !wqHearingList.isEmpty()) {
            wqHearingDTO = wqHearingList.get(0);
        }
        return wqHearingDTO;
    }

    public int findWQLinkRegisterByMaatId(int maatId) {
        int caseId = 0;
        List<WQLinkRegisterDTO> wqLinkRegisterList = maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getWqLinkRegisterEndpoints().getFindUrl(),
                emptyMap(),
                maatId
        );
        if (wqLinkRegisterList != null && !wqLinkRegisterList.isEmpty()) {
            caseId = wqLinkRegisterList.get(0).getCaseId();
        }
        return caseId;
    }

    public List<OffenceDTO> findOffenceByCaseId(int caseId) {
        return maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getWqLinkRegisterEndpoints().getFindUrl(),
                emptyMap(),
                caseId
        );
    }

    public int getOffenceNewOffenceCount(int caseId, String offenceId) {
        int count = 0;
        List<Integer> offenceCount = maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getOffenceEndpoints().getOffenceCountUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
        if (offenceCount != null && !offenceCount.isEmpty()) {
            count = offenceCount.get(0);
        }
        return count;
    }


    public int getWQOffenceNewOffenceCount(int caseId, String offenceId) {
        int count = 0;
        List<Integer> offenceCount = maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getWqOffenceEndpoints().getWqOffenceCountUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
        if (offenceCount != null && !offenceCount.isEmpty()) {
            count = offenceCount.get(0);
        }
        return count;
    }

    public List<Integer> findResultsByWQTypeSubType(int wqType, int subTypeCode) {
        return maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getXlatResultEndpoints().getResultCodesForWQTypeSubTypeUrl(),
                emptyMap(),
                wqType,
                subTypeCode
        );
    }

    public List<Integer> getResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        return maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getResultEndpoints().getResultCodeByCaseIdAndAsnSeqUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
    }

    public List<Integer> getWqResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        return maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getWqResultEndpoints().getResultCodeByCaseIdAndAsnSeqUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
    }

    public List<Integer> fetchResultCodesForCCImprisonment() {
        return maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getXlatResultEndpoints().getResultCodesForCCImprisonmentUrl(),
                emptyMap()
        );
    }

    public List<Integer> findByCjsResultCodeIn() {
        return maatCourtDataClient.getApiResponseViaGET(
                List.class,
                configuration.getXlatResultEndpoints().getResultCodesForCCBenchWarrantUrl(),
                emptyMap()
        );
    }

    public RepOrderDTO getRepOrder(Integer repId) {
        RepOrderDTO response = maatCourtDataClient.getApiResponseViaGET(
                RepOrderDTO.class,
                configuration.getRepOrderEndpoints().getFindUrl(),
                repId
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }

    public void updateCrownCourtOutcome(UpdateCCOutcome updateCCOutcome) {
        maatCourtDataClient.getApiResponseViaGET(
                RepOrderDTO.class,
                configuration.getCrownCourtStoredProcedureEndpoints().getUpdateCrownCourtOutcomeUrl(),
                updateCCOutcome
        );
    }

    public void invokeUpdateAppealSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatCourtDataClient.getApiResponseViaGET(
                RepOrderDTO.class,
                configuration.getCrownCourtProcessingEndpoints().getUpdateAppealCcSentenceUrl(),
                updateSentenceOrder
        );
    }

    public void invokeUpdateSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatCourtDataClient.getApiResponseViaGET(
                RepOrderDTO.class,
                configuration.getCrownCourtProcessingEndpoints().getUpdateCcSentenceUrl(),
                updateSentenceOrder
        );
    }

    public Boolean isMaatRecordLocked(Integer maatId) {
        return maatCourtDataClient.getApiResponseViaGET(
                Boolean.class,
                configuration.getReservationsEndpoints().getIsMaatRecordLockedUrl(),
                maatId
        );
    }

}
