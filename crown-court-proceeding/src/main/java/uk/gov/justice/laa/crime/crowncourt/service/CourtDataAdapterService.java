package uk.gov.justice.laa.crime.crowncourt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.crime.commons.client.RestAPIClient;
import uk.gov.justice.laa.crime.crowncourt.config.ServicesConfiguration;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtDataAdapterService {

    @Qualifier("cdaApiNonServletClient")
    private final RestAPIClient cdaApiNonServletClient;
    private final ServicesConfiguration configuration;


    public void triggerHearingProcessing(UUID hearingId) {
        log.info("Triggering processing for hearing '{}' via court data adapter.", hearingId);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("publish_to_queue", "true");

        cdaApiNonServletClient.get(
                new ParameterizedTypeReference<Void>() {},
                configuration.getCourtDataAdapter().getHearingUrl(),
                Collections.emptyMap(),
                queryParams,
                hearingId
        );
        log.info("Completed triggering processing for hearing '{}' via court data adapter.", hearingId);
    }
}
