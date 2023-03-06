package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.client.MaatAPIClient;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
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

    private final MaatAPIClient maatAPIClient;
    private final ServicesConfiguration configuration;
    public static final String RESPONSE_STRING = "Response from Court Data API: %s";

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
        IOJAppealDTO response = maatAPIClient.getApiResponseViaGET(
                IOJAppealDTO.class,
                configuration.getMaatApi().getIojAppealEndpoints().getFindUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId),
                repId
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }

    public RepOrderDTO updateRepOrder(UpdateRepOrderRequestDTO updateRepOrderRequestDTO, String laaTransactionId) {
        RepOrderDTO response =  maatAPIClient.getApiResponseViaPUT(
                updateRepOrderRequestDTO,
                RepOrderDTO.class,
                configuration.getMaatApi().getRepOrderEndpoints().getUpdateUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId)
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }

    public Object getRepOrderByFilter(String repId, String sentenceOrdDate) throws IOException {
        Map<String, Object> graphQLBody = getGraphQLRequestBody(repId, sentenceOrdDate);
        Object response = maatAPIClient.getGraphQLApiResponse(
                Object.class,
                configuration.getMaatApi().getGraphQLEndpoints().getGraphqlQueryUrl(),
                graphQLBody
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }

    public List<RepOrderCCOutcomeDTO> getRepOrderCCOutcomeByRepId(Integer repId, String laaTransactionId) {
        List<RepOrderCCOutcomeDTO> response = maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getRepOrderEndpoints().getFindOutcomeUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId),
                repId
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }

    public WQHearingDTO retrieveHearingForCaseConclusion(ProsecutionConcluded prosecutionConcluded) {

        WQHearingDTO wqHearingDTO = null;
        List<WQHearingDTO> wqHearingList = maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getWqHearingEndpoints().getFindUrl(),
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
        List<WQLinkRegisterDTO> wqLinkRegisterList = maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getWqLinkRegisterEndpoints().getFindUrl(),
                emptyMap(),
                maatId
        );
        if (wqLinkRegisterList != null && !wqLinkRegisterList.isEmpty()) {
            caseId = wqLinkRegisterList.get(0).getCaseId();
        }
        return caseId;
    }

    public List<OffenceDTO> findOffenceByCaseId(int caseId) {
        return maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getWqLinkRegisterEndpoints().getFindUrl(),
                emptyMap(),
                caseId
        );
    }

    public int getOffenceNewOffenceCount(int caseId, String offenceId) {
        int count = 0;
        List<Integer> offenceCount = maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getOffenceEndpoints().getOffenceCountUrl(),
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
        List<Integer> offenceCount = maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getWqOffenceEndpoints().getWqOffenceCountUrl(),
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
        return maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getXlatResultEndpoints().getResultCodesForWQTypeSubTypeUrl(),
                emptyMap(),
                wqType,
                subTypeCode
        );
    }

    public List<Integer> getResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        return maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getResultEndpoints().getResultCodeByCaseIdAndAsnSeqUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
    }

    public List<Integer> getWqResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        return maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getWqResultEndpoints().getResultCodeByCaseIdAndAsnSeqUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
    }

    public List<Integer> fetchResultCodesForCCImprisonment() {
        return maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getXlatResultEndpoints().getResultCodesForCCImprisonmentUrl(),
                emptyMap()
        );
    }

    public List<Integer> findByCjsResultCodeIn() {
        return maatAPIClient.getApiResponseViaGET(
                List.class,
                configuration.getMaatApi().getXlatResultEndpoints().getResultCodesForCCBenchWarrantUrl(),
                emptyMap()
        );
    }

    public RepOrderDTO getRepOrder(Integer repId) {
        RepOrderDTO response = maatAPIClient.getApiResponseViaGET(
                RepOrderDTO.class,
                configuration.getMaatApi().getRepOrderEndpoints().getFindUrl(),
                repId
        );
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }

    public void updateCrownCourtOutcome(UpdateCCOutcome updateCCOutcome) {
        maatAPIClient.getApiResponseViaGET(
                RepOrderDTO.class,
                configuration.getMaatApi().getCrownCourtStoredProcedureEndpoints().getUpdateCrownCourtOutcomeUrl(),
                updateCCOutcome
        );
    }

    public void invokeUpdateAppealSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatAPIClient.getApiResponseViaGET(
                RepOrderDTO.class,
                configuration.getMaatApi().getCrownCourtProcessingEndpoints().getUpdateAppealCcSentenceUrl(),
                updateSentenceOrder
        );
    }

    public void invokeUpdateSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatAPIClient.getApiResponseViaGET(
                RepOrderDTO.class,
                configuration.getMaatApi().getCrownCourtProcessingEndpoints().getUpdateCcSentenceUrl(),
                updateSentenceOrder
        );
    }

    public Boolean isMaatRecordLocked(Integer maatId) {
        return maatAPIClient.getApiResponseViaGET(
                Boolean.class,
                configuration.getMaatApi().getReservationEndpoints().getIsMaatRecordLockedUrl(),
                maatId
        );
    }

    public RepOrderCCOutcomeDTO createOutcome(RepOrderCCOutcomeDTO outcomeDTO, String laaTransactionId) {
        RepOrderCCOutcomeDTO response = maatAPIClient.getApiResponseViaPUT(
                outcomeDTO,
                RepOrderCCOutcomeDTO.class,
                configuration.getMaatApi().getRepOrderEndpoints().getCreateOutcomeUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId));
        log.info(String.format(RESPONSE_STRING, response));
        return response;
    }
}
