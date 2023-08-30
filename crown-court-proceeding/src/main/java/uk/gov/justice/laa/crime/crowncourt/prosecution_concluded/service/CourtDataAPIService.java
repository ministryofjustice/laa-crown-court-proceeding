package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.OffenceDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQLinkRegisterDTO;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateSentenceOrder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtDataAPIService {

    @Qualifier("maatApiNonServletClient")
    private final RestAPIClient maatAPIClient;
    private final ServicesConfiguration configuration;
    public static final String RESPONSE_STRING = "Response from Court Data API: {}";


    public RepOrderDTO getRepOrder(Integer repId) {
        RepOrderDTO response = maatAPIClient.get(
                new ParameterizedTypeReference<RepOrderDTO>() {},
                configuration.getMaatApi().getRepOrderEndpoints().getFindUrl(),
                repId
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public void updateCrownCourtOutcome(UpdateCCOutcome updateCCOutcome) {
        maatAPIClient.get(
                new ParameterizedTypeReference<RepOrderDTO>() {},
                configuration.getMaatApi().getCrownCourtStoredProcedureEndpoints().getUpdateCrownCourtOutcomeUrl(),
                updateCCOutcome
        );
    }

    public WQHearingDTO retrieveHearingForCaseConclusion(ProsecutionConcluded prosecutionConcluded) {

        WQHearingDTO wqHearingDTO = null;
        List<WQHearingDTO> wqHearingList = maatAPIClient.get(
                new ParameterizedTypeReference<List<WQHearingDTO>>() {},
                configuration.getMaatApi().getWqHearingEndpoints().getFindUrl(),
                Map.of(Constants.LAA_TRANSACTION_ID, UUID.randomUUID().toString()),
                prosecutionConcluded.getHearingIdWhereChangeOccurred().toString(),
                prosecutionConcluded.getMaatId()
        );
        if (wqHearingList != null && !wqHearingList.isEmpty()) {
            wqHearingDTO = wqHearingList.get(0);
        }
        return wqHearingDTO;
    }

    public int findWQLinkRegisterByMaatId(int maatId) {
        int caseId = 0;
        List<WQLinkRegisterDTO> wqLinkRegisterList = maatAPIClient.get(
                new ParameterizedTypeReference<List<WQLinkRegisterDTO>>() {},
                configuration.getMaatApi().getWqLinkRegisterEndpoints().getFindUrl(),
                emptyMap(),
                maatId
        );
        log.info(RESPONSE_STRING, wqLinkRegisterList);
        if (wqLinkRegisterList != null && !wqLinkRegisterList.isEmpty()) {
            caseId = wqLinkRegisterList.get(0).getCaseId();
        }
        return caseId;
    }

    public List<OffenceDTO> findOffenceByCaseId(int caseId) {
        List<OffenceDTO> response = maatAPIClient.get(
                new ParameterizedTypeReference<List<OffenceDTO>>() {},
                configuration.getMaatApi().getWqLinkRegisterEndpoints().getFindUrl(),
                emptyMap(),
                caseId
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public int getOffenceNewOffenceCount(int caseId, String offenceId) {
        int count = 0;
        List<Integer> offenceCount = maatAPIClient.get(
                new ParameterizedTypeReference<List<Integer>>() {},
                configuration.getMaatApi().getOffenceEndpoints().getOffenceCountUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
        log.info(RESPONSE_STRING, offenceCount);
        if (offenceCount != null && !offenceCount.isEmpty()) {
            count = offenceCount.get(0);
        }
        return count;
    }

    public int getWQOffenceNewOffenceCount(int caseId, String offenceId) {
        int count = 0;
        List<Integer> offenceCount = maatAPIClient.get(
                new ParameterizedTypeReference<List<Integer>>() {},
                configuration.getMaatApi().getWqOffenceEndpoints().getWqOffenceCountUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
        log.info(RESPONSE_STRING, offenceCount);
        if (offenceCount != null && !offenceCount.isEmpty()) {
            count = offenceCount.get(0);
        }
        return count;
    }

    public List<Integer> findResultsByWQTypeSubType(int wqType, int subTypeCode) {
        List<Integer> response = maatAPIClient.get(
                new ParameterizedTypeReference<List<Integer>>() {},
                configuration.getMaatApi().getXlatResultEndpoints().getResultCodesForWQTypeSubTypeUrl(),
                emptyMap(),
                wqType,
                subTypeCode
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<Integer> getResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        List<Integer> response = maatAPIClient.get(
                new ParameterizedTypeReference<List<Integer>>() {},
                configuration.getMaatApi().getResultEndpoints().getResultCodeByCaseIdAndAsnSeqUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<Integer> getWqResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        List<Integer> response = maatAPIClient.get(
                new ParameterizedTypeReference<List<Integer>>() {},
                configuration.getMaatApi().getWqResultEndpoints().getResultCodeByCaseIdAndAsnSeqUrl(),
                emptyMap(),
                caseId,
                offenceId
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<Integer> fetchResultCodesForCCImprisonment() {
        List<Integer> response = maatAPIClient.get(
                new ParameterizedTypeReference<List<Integer>>() {},
                configuration.getMaatApi().getXlatResultEndpoints().getResultCodesForCCImprisonmentUrl(),
                emptyMap()
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<Integer> findByCjsResultCodeIn() {
        List<Integer> response = maatAPIClient.get(
                new ParameterizedTypeReference<List<Integer>>() {},
                configuration.getMaatApi().getXlatResultEndpoints().getResultCodesForCCBenchWarrantUrl(),
                emptyMap()
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public Boolean isMaatRecordLocked(Integer maatId) {
        Boolean response = maatAPIClient.get(
                new ParameterizedTypeReference<Boolean>() {},
                configuration.getMaatApi().getReservationEndpoints().getIsMaatRecordLockedUrl(),
                maatId
        );
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public void invokeUpdateAppealSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatAPIClient.get(
                new ParameterizedTypeReference<RepOrderDTO>() {},
                configuration.getMaatApi().getCrownCourtProcessingEndpoints().getUpdateAppealCcSentenceUrl(),
                updateSentenceOrder
        );
    }

    public void invokeUpdateSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatAPIClient.get(
                new ParameterizedTypeReference<RepOrderDTO>() {},
                configuration.getMaatApi().getCrownCourtProcessingEndpoints().getUpdateCcSentenceUrl(),
                updateSentenceOrder
        );
    }
}
