package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.*;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateSentenceOrder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

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
                IOJAppealDTO.class,
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
                RepOrderDTO.class,
                configuration.getMaatApi().getRepOrderEndpoints().getUpdateUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId)
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<RepOrderCCOutcomeDTO> getRepOrderCCOutcomeByRepId(Integer repId, String laaTransactionId) {
        List<RepOrderCCOutcomeDTO> response = maatAPIClient.get(
                List.class,
                configuration.getMaatApi().getRepOrderEndpoints().getFindOutcomeUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, laaTransactionId),
                repId
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public WQHearingDTO retrieveHearingForCaseConclusion(ProsecutionConcluded prosecutionConcluded) {

        WQHearingDTO wqHearingDTO = null;
        List<WQHearingDTO> wqHearingList = maatAPIClient.get(
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
        List<WQLinkRegisterDTO> wqLinkRegisterList = maatAPIClient.get(
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
        return maatAPIClient.get(
                List.class,
                configuration.getMaatApi().getWqLinkRegisterEndpoints().getFindUrl(),
                emptyMap(),
                caseId
        );
    }

    public int getOffenceNewOffenceCount(int caseId, String offenceId) {
        int count = 0;
        List<Integer> offenceCount = maatAPIClient.get(
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
        List<Integer> offenceCount = maatAPIClient.get(
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
        return maatAPIClient.get(
                List.class,
                configuration.getMaatApi().getXlatResultEndpoints().getResultCodesForWQTypeSubTypeUrl(),
                emptyMap(),
                wqType,
                subTypeCode
        );
    }

    public List<Integer> getResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        return maatAPIClient.get(
                List.class,
                configuration.getMaatApi().getResultEndpoints().getResultCodeByCaseIdAndAsnSeqUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
    }

    public List<Integer> getWqResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        return maatAPIClient.get(
                List.class,
                configuration.getMaatApi().getWqResultEndpoints().getResultCodeByCaseIdAndAsnSeqUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
    }

    public List<Integer> fetchResultCodesForCCImprisonment() {
        return maatAPIClient.get(
                List.class,
                configuration.getMaatApi().getXlatResultEndpoints().getResultCodesForCCImprisonmentUrl(),
                emptyMap()
        );
    }

    public List<Integer> findByCjsResultCodeIn() {
        return maatAPIClient.get(
                List.class,
                configuration.getMaatApi().getXlatResultEndpoints().getResultCodesForCCBenchWarrantUrl(),
                emptyMap()
        );
    }

    public RepOrderDTO getRepOrder(Integer repId) {
        RepOrderDTO response = maatAPIClient.get(
                RepOrderDTO.class,
                configuration.getMaatApi().getRepOrderEndpoints().getFindUrl(),
                repId
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public void updateCrownCourtOutcome(UpdateCCOutcome updateCCOutcome) {
        maatAPIClient.get(
                RepOrderDTO.class,
                configuration.getMaatApi().getCrownCourtStoredProcedureEndpoints().getUpdateCrownCourtOutcomeUrl(),
                updateCCOutcome
        );
    }

    public void invokeUpdateAppealSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatAPIClient.get(
                RepOrderDTO.class,
                configuration.getMaatApi().getCrownCourtProcessingEndpoints().getUpdateAppealCcSentenceUrl(),
                updateSentenceOrder
        );
    }

    public void invokeUpdateSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatAPIClient.get(
                RepOrderDTO.class,
                configuration.getMaatApi().getCrownCourtProcessingEndpoints().getUpdateCcSentenceUrl(),
                updateSentenceOrder
        );
    }

    public Boolean isMaatRecordLocked(Integer maatId) {
        return maatAPIClient.get(
                Boolean.class,
                configuration.getMaatApi().getReservationEndpoints().getIsMaatRecordLockedUrl(),
                maatId
        );
    }

    public RepOrderCCOutcomeDTO createOutcome(RepOrderCCOutcomeDTO outcomeDTO, String laaTransactionId) {
        RepOrderCCOutcomeDTO response = maatAPIClient.put(
                outcomeDTO,
                RepOrderCCOutcomeDTO.class,
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
}
