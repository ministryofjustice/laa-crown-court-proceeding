package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtDataAdapterService {

    @Qualifier("cdaApiClient")
    private final RestAPIClient cdaAPIClient;
    private final ServicesConfiguration configuration;


    public void triggerHearingProcessing(UUID hearingId, String laaTransactionId) {
        log.info("Triggering processing for hearing '{}' via court data adapter.", hearingId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("publish_to_queue", "true");

        cdaAPIClient.get(
                Void.class,
                configuration.getCourtDataAdapter().getHearingUrl(),
                Map.of("X-Request-ID", laaTransactionId),
                queryParams,
                hearingId
        );
    }
}
