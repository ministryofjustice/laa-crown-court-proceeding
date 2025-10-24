package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.client.MaatCourtDataApiClient;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaatCourtDataService {

    private final MaatCourtDataApiClient maatAPIClient;
    public static final String RESPONSE_STRING = "Response from Court Data API: {}";

    public IOJAppealDTO getCurrentPassedIOJAppealFromRepId(Integer repId) {
        log.debug("Request to get current passed IOJ appeal for repId: {}", repId);
        IOJAppealDTO response = maatAPIClient.getCurrentPassedIOJAppeal(repId);
        log.debug(RESPONSE_STRING, response);
        return response;
    }

    public RepOrderDTO updateRepOrder(UpdateRepOrderRequestDTO updateRepOrderRequestDTO) {
        log.debug("Request to update rep order: {}", updateRepOrderRequestDTO);
        RepOrderDTO response = maatAPIClient.updateRepOrder(updateRepOrderRequestDTO);
        log.debug(RESPONSE_STRING, response);
        return response;
    }

    public List<RepOrderCCOutcomeDTO> getRepOrderCCOutcomeByRepId(Integer repId) {
        log.debug("Request to get rep order CC outcome for repId: {}", repId);
        List<RepOrderCCOutcomeDTO> response = maatAPIClient.getRepOrderCCOutcomeByRepId(repId);
        log.debug(RESPONSE_STRING, response);
        return response;
    }

    public RepOrderCCOutcomeDTO createOutcome(RepOrderCCOutcomeDTO outcomeDTO) {
        log.debug("Request to create rep order CC outcome: {}", outcomeDTO);
        RepOrderCCOutcomeDTO response = maatAPIClient.createCrownCourtOutcome(outcomeDTO);
        log.debug(RESPONSE_STRING, response);
        return response;
    }

    public long outcomeCount(Integer repId) {
        log.debug("Request to get outcome count for repId: {}", repId);
        ResponseEntity<Void> response = maatAPIClient.getOutcomeCount(repId);
        log.debug(RESPONSE_STRING, response);
        String header = response.getHeaders().getFirst("X-Total-Records");
        return header == null ? 0 : Long.parseLong(header);
    }
}
