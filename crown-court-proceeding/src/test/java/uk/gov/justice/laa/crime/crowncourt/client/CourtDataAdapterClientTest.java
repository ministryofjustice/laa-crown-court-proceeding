package uk.gov.justice.laa.crime.crowncourt.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.crime.crowncourt.config.CourtDataAdapterClientConfig;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.exception.CCPDataException;
import uk.gov.justice.laa.crime.crowncourt.model.laastatus.LaaStatusUpdate;
import uk.gov.justice.laa.crime.crowncourt.model.laastatus.RepOrderData;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtDataAdapterClientTest {

    private final String hearingUrl = "cda-test/hearing/{hearingId}";
    private final String baseUrl = "http://localhost:1234/";

    @Mock
    private ExchangeFunction shortCircuitExchangeFunction;
    @Mock
    private CourtDataAdapterClientConfig courtDataAdapterClientConfig;
    @Mock
    private QueueMessageLogService queueMessageLogService;
    @Captor
    ArgumentCaptor<ClientRequest> requestCaptor;

    private CourtDataAdapterClient courtDataAdapterClient;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        WebClient testWebClient = WebClient
                .builder()
                .baseUrl(baseUrl)
                .exchangeFunction(shortCircuitExchangeFunction)
                .build();

        courtDataAdapterClient = new CourtDataAdapterClient(testWebClient, queueMessageLogService, courtDataAdapterClientConfig);
    }

    @Test
    void givenAValidLaaStatusObject_whenPostLaaStatusIsInvoked_thenTheRequestIsSentCorrectly() throws JsonProcessingException {
        String laaStatusUrl = "cda-test/laaStatus";
        when(courtDataAdapterClientConfig.getLaaStatusUrl()).thenReturn(laaStatusUrl);
        Map<String, String> headers = Map.of("test-header", "test-header-value");
        LaaStatusUpdate testStatusObject = getTestLaaStatusObject();

        courtDataAdapterClient.postLaaStatus(testStatusObject, headers);
        String jsonBody = objectMapper.writeValueAsString(testStatusObject);
        verify(queueMessageLogService, atLeastOnce())
                .createLog(MessageType.LAA_STATUS_UPDATE, jsonBody);
    }

    @Test
    void givenAValidHearingId_whenTriggerHearingProcessingIsInvoked_thenTheRequestIsSentCorrectly() {
        when(shortCircuitExchangeFunction.exchange(requestCaptor.capture()))
                .thenReturn(Mono.just(ClientResponse.create(HttpStatus.OK).build()));
        when(courtDataAdapterClientConfig.getHearingUrl()).thenReturn(hearingUrl);

        UUID testHearingId = UUID.randomUUID();
        String testTransactionId = UUID.randomUUID().toString();
        courtDataAdapterClient.triggerHearingProcessing(testHearingId, testTransactionId);

        validateTriggerHearingProcessingScenario(testHearingId, testTransactionId);
    }

    @Test
    void givenAValidHearingId_whenTriggerHearingProcessingIsInvokedAndTheCallFails_thenFailureIsHandled() {
        when(shortCircuitExchangeFunction.exchange(requestCaptor.capture()))
                .thenReturn(Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build()));

        when(courtDataAdapterClientConfig.getHearingUrl()).thenReturn(hearingUrl);

        UUID testHearingId = UUID.randomUUID();
        String testTransactionId = UUID.randomUUID().toString();
        assertThatThrownBy(() -> courtDataAdapterClient.triggerHearingProcessing(testHearingId, testTransactionId))
                .isInstanceOf(CCPDataException.class)
                .hasMessageContaining((String.format("Error triggering CDA processing for hearing '%s'.", testHearingId)));
        validateTriggerHearingProcessingScenario(testHearingId, testTransactionId);
    }

    private void validateTriggerHearingProcessingScenario(UUID testHearingId, String laaTransactionId) {
        Map<String, String> expectedFinalHeaders = Map.of("X-Request-ID", laaTransactionId);
        String expectedUrl = String.format("%s?publish_to_queue=true", hearingUrl);
        expectedUrl = expectedUrl.replace("{hearingId}", testHearingId.toString());
        validateRequest(requestCaptor.getValue(), expectedUrl, expectedFinalHeaders);
    }

    private void validateRequest(ClientRequest request, String expectedUrl, Map<String, String> expectedHeaders) {
        assertThat(request.headers().toSingleValueMap()).isEqualTo(expectedHeaders);
        assertThat(request.url()).hasToString(String.format("%s%s", baseUrl, expectedUrl));
        assertThat(request.method()).isEqualTo(HttpMethod.GET);
    }

    private LaaStatusUpdate getTestLaaStatusObject() {
        return LaaStatusUpdate.builder().data(RepOrderData.builder().type("test-representation_order").build()).build();
    }
}
