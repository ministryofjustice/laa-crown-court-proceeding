package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.OffenceDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQLinkRegisterDTO;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateCCOutcome;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateSentenceOrder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.client.MaatCourtDataNonServletApiClient;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.service.CourtDataAdapterService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtDataAPIService {
    
    private final MaatCourtDataNonServletApiClient maatAPIClient;
    private final CourtDataAdapterService courtDataAdapterService;
    public static final String RESPONSE_STRING = "Response from Court Data API: {}";


    public RepOrderDTO getRepOrder(Integer repId) {
        RepOrderDTO response = maatAPIClient.getRepOrderByRepId(repId);
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public void updateCrownCourtOutcome(UpdateCCOutcome updateCCOutcome) {
        maatAPIClient.updateCrownCourtOutcome(updateCCOutcome);
    }

    public WQHearingDTO retrieveHearingForCaseConclusion(ProsecutionConcluded prosecutionConcluded) {

        List<WQHearingDTO> wqHearingList = maatAPIClient.getWorkQueueHearing(
            prosecutionConcluded.getHearingIdWhereChangeOccurred().toString(), 
            prosecutionConcluded.getMaatId());

        WQHearingDTO wqHearingDTO = CollectionUtils.isNotEmpty(wqHearingList) ? wqHearingList.get(0) : null;
        if (wqHearingDTO == null
                && prosecutionConcluded.isConcluded()) {
            courtDataAdapterService.
                    triggerHearingProcessing(
                            prosecutionConcluded.getHearingIdWhereChangeOccurred()
                    );
        }
        return wqHearingDTO;
    }

    public int findWQLinkRegisterByMaatId(int maatId) {
        int caseId = 0;

        List<WQLinkRegisterDTO> wqLinkRegisterList = maatAPIClient.getWorkQueueLinkRegister(maatId);

        log.info(RESPONSE_STRING, wqLinkRegisterList);
        if (wqLinkRegisterList != null && !wqLinkRegisterList.isEmpty()) {
            caseId = wqLinkRegisterList.get(0).getCaseId();
        }
        return caseId;
    }

    public List<OffenceDTO> findOffenceByCaseId(int caseId) {
        List<OffenceDTO> response = maatAPIClient.getOffenceByCaseId(caseId);
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public long getOffenceNewOffenceCount(int caseId, String offenceId) {
        Integer response = maatAPIClient.getOffenceNewOffenceCount(offenceId, caseId);
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public long getWQOffenceNewOffenceCount(int caseId, String offenceId) {
        Integer response = maatAPIClient.getWorkQueueOffenceCount(offenceId, caseId);
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<Integer> findResultsByWQTypeSubType(int wqType, int subTypeCode) {
        List<Integer> response = maatAPIClient.getResultsByWorkQueueTypeSubType(wqType, subTypeCode);
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<Integer> getResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        List<Integer> response = maatAPIClient.getResultCodeByCaseIdAndAsnSeq(caseId, offenceId);
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<Integer> getWqResultCodeByCaseIdAndAsnSeq(int caseId, String offenceId) {
        List<Integer> response = maatAPIClient.getWorkQueueResultCodeByCaseIdAndAsnSeq(caseId, offenceId);
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<Integer> fetchResultCodesForCCImprisonment() {
        List<Integer> response = maatAPIClient.getResultCodesForCrownCourtImprisonment();
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public List<Integer> findByCjsResultCodeIn() {
        List<Integer> response = maatAPIClient.getResultCodesForCrownCourtBenchWarrantUrl();
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public Boolean isMaatRecordLocked(Integer maatId) {
        Boolean response = maatAPIClient.isMaatRecordLocked(maatId);
        log.info(RESPONSE_STRING, response);
        return response;
    }

    public void invokeUpdateAppealSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatAPIClient.updateAppealSentenceOrderDate(updateSentenceOrder);
    }

    public void invokeUpdateSentenceOrderDate(UpdateSentenceOrder updateSentenceOrder) {
        maatAPIClient.updateSentenceOrderDate(updateSentenceOrder);
    }

    public List<RepOrderCCOutcomeDTO> getRepOrderCCOutcomeByRepId(Integer repId) {
        List<RepOrderCCOutcomeDTO> response = maatAPIClient.getRepOrderCCOutcomeByRepId(repId);
        log.info(RESPONSE_STRING, response);
        return response;
    }
}
