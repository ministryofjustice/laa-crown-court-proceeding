package uk.gov.justice.laa.crime.crowncourt.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.laa.crime.crowncourt.config.CourtDataAdapterClientConfig;
import uk.gov.justice.laa.crime.crowncourt.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.exception.CCPDataException;
import uk.gov.justice.laa.crime.crowncourt.model.laastatus.LaaStatusUpdate;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtDataAdapterClient {

    @Autowired
    @Qualifier("cdaOAuth2WebClient")
    private final WebClient webClient;

    ObjectMapper objectMapper = new ObjectMapper();

    private final QueueMessageLogService queueMessageLogService;

    private final CourtDataAdapterClientConfig courtDataAdapterClientConfig;

    /**
     * @param laaStatusUpdate laa status value
     */
    public void postLaaStatus(LaaStatusUpdate laaStatusUpdate, Map<String, String> headers) throws JsonProcessingException {


        final String laaStatusUpdateJson = objectMapper.writeValueAsString(laaStatusUpdate);
        queueMessageLogService.createLog(MessageType.LAA_STATUS_UPDATE, laaStatusUpdateJson);

        log.info("Post Laa status to CDA.");
        webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path(courtDataAdapterClientConfig.getLaaStatusUrl()).build())
                .headers(httpHeaders -> httpHeaders.setAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(laaStatusUpdateJson))
                .retrieve();
    }

    public void triggerHearingProcessing(UUID hearingId, String laaTransactionId) {
        log.info("Triggering processing for hearing '{}' via court data adapter.", hearingId);
        webClient
                .get()
                .uri(uriBuilder ->
                        uriBuilder.path(courtDataAdapterClientConfig.getHearingUrl()).queryParam("publish_to_queue", true).build(hearingId))
                .headers(httpHeaders -> httpHeaders.setAll(Map.of("X-Request-ID", laaTransactionId)))
                .retrieve().toBodilessEntity()
                .doOnError(Sentry::captureException)
                .onErrorMap(error -> new CCPDataException(String.format("Error triggering CDA processing for hearing '%s'.%s", hearingId, error.getMessage())))
                .doOnSuccess(response -> log.info("Processing trigger successfully for hearing '{}'", hearingId))
                .block();
    }
}
