package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.client.CourtDataAdaptorNonServletApiClient;

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
}
