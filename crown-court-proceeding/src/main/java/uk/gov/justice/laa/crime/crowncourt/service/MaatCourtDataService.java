package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaatCourtDataService {

    @Qualifier("maatApiClient")
    private final RestAPIClient maatAPIClient;
    private final ServicesConfiguration configuration;
    public static final String RESPONSE_STRING = "Response from Court Data API: {}";

    public IOJAppealDTO getCurrentPassedIOJAppealFromRepId(Integer repId) {
        log.debug("Request to get current passed IOJ appeal for repId: {}", repId);
        IOJAppealDTO response = maatAPIClient.get(
                new ParameterizedTypeReference<IOJAppealDTO>() {
                },
                configuration.getMaatApi().getIojAppealEndpoints().getFindUrl(),
                Collections.emptyMap(),
                repId
        );
        log.debug(RESPONSE_STRING, response);
        return response;
    }

    public RepOrderDTO updateRepOrder(UpdateRepOrderRequestDTO updateRepOrderRequestDTO) {
        log.debug("Request to update rep order: {}", updateRepOrderRequestDTO);
        RepOrderDTO response = maatAPIClient.put(
                updateRepOrderRequestDTO,
                new ParameterizedTypeReference<RepOrderDTO>() {
                },
                configuration.getMaatApi().getRepOrderEndpoints().getUpdateUrl(),
                Collections.emptyMap()
        );
        log.debug(RESPONSE_STRING, response);
        return response;
    }

    public List<RepOrderCCOutcomeDTO> getRepOrderCCOutcomeByRepId(Integer repId) {
        log.debug("Request to get rep order CC outcome for repId: {}", repId);
        List<RepOrderCCOutcomeDTO> response = maatAPIClient.get(
                new ParameterizedTypeReference<List<RepOrderCCOutcomeDTO>>() {
                },
                configuration.getMaatApi().getRepOrderEndpoints().getFindOutcomeUrl(),
                Collections.emptyMap(),
                repId
        );
        log.debug(RESPONSE_STRING, response);
        return response;
    }

    public RepOrderCCOutcomeDTO createOutcome(RepOrderCCOutcomeDTO outcomeDTO) {
        log.debug("Request to create rep order CC outcome: {}", outcomeDTO);
        RepOrderCCOutcomeDTO response = maatAPIClient.post(
                outcomeDTO,
                new ParameterizedTypeReference<RepOrderCCOutcomeDTO>() {
                },
                configuration.getMaatApi().getRepOrderEndpoints().getCreateOutcomeUrl(),
                Collections.emptyMap()
        );
        log.debug(RESPONSE_STRING, response);
        return response;
    }

    public long outcomeCount(Integer repId) {
        log.debug("Request to get outcome count for repId: {}", repId);
        ResponseEntity<Void> response = maatAPIClient.head(
                configuration.getMaatApi().getRepOrderEndpoints().getFindOutcomeUrl(),
                Collections.emptyMap(),
                repId
        );
        log.debug(RESPONSE_STRING, response);
        String header = response.getHeaders().getFirst("X-Total-Records");
        return header == null ? 0 : Long.parseLong(header);
    }
}
