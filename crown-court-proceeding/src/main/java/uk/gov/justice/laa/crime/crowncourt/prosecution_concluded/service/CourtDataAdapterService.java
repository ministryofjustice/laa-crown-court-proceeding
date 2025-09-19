package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.WQHearingDTO;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.builder.WQHearingDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.client.CourtDataAdaptorNonServletApiClient;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.HearingResultResponse;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtDataAdapterService {
    
    private final CourtDataAdaptorNonServletApiClient courtDataAdaptorApiClient;
    
    public void triggerHearingProcessing(UUID hearingId) {
        log.info("Triggering processing for hearing '{}' via court data adapter.", hearingId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("publish_to_queue", "true");

        courtDataAdaptorApiClient.triggerHearingProcessing(hearingId, queryParams);
        log.info("Completed triggering processing for hearing '{}' via court data adapter.", hearingId);
    }

    public WQHearingDTO getHearingResult(ProsecutionConcluded prosecutionConcluded) {
        log.info("Getting hearing result'{}' via court data adapter.", prosecutionConcluded.getHearingIdWhereChangeOccurred());

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("publish_to_queue", "false");

        HearingResultResponse hearingResultResponse = courtDataAdaptorApiClient.getHearingResult(prosecutionConcluded.getHearingIdWhereChangeOccurred(), queryParams);
        log.info("Completed getting hearing result '{}' via court data adapter.", prosecutionConcluded.getHearingIdWhereChangeOccurred());
        return WQHearingDTOBuilder.build(hearingResultResponse, prosecutionConcluded);
    }
}
