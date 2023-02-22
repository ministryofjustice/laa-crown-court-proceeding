package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.crime.crowncourt.client.CourtDataAdapterClient;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtDataAdapterService {

    private final ServicesConfiguration configuration;
    private final CourtDataAdapterClient courtDataAdapterClient;


    public void triggerHearingProcessing(UUID hearingId, String laaTransactionId) {
        log.info("Triggering processing for hearing '{}' via court data adapter.", hearingId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("publish_to_queue", "true");

        courtDataAdapterClient.getApiResponseViaGET(
                Void.class,
                configuration.getCourtDataAdapter().getHearingUrl(),
                Map.of("X-Request-ID", laaTransactionId),
                queryParams,
                hearingId
        );
    }
}
