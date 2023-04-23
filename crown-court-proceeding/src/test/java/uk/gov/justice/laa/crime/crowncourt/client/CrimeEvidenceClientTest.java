package uk.gov.justice.laa.crime.crowncourt.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@ExtendWith(MockitoExtension.class)
class CrimeEvidenceClientTest {

    public static final String MOCK_URL = "mock-url";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String LAA_TRANSACTION_ID = "laaTransactionId";
    private final Integer REP_ID = 1234;
    private WebClient testWebClient;
    private CrimeEvidenceClient crimeEvidenceClient;

    @Mock
    private ExchangeFunction shortCircuitExchangeFunction;


    @BeforeEach
    void setup() {
        testWebClient = WebClient
                .builder()
                .baseUrl("http://localhost:1234")
                .filter(ExchangeFilterFunctions.statusError(
                                HttpStatus::is4xxClientError,
                                r -> WebClientResponseException.create(
                                        r.rawStatusCode(), r.statusCode().getReasonPhrase(), null, null, null
                                )
                        )
                )
                .exchangeFunction(shortCircuitExchangeFunction)
                .build();

        crimeEvidenceClient = Mockito.spy(new CrimeEvidenceClient(testWebClient));
    }

    @Test
    void givenCrimeEvidenceClient_whenGetWebClientIsInvoked_thenWebClientIsReturned() {
        assertThat(crimeEvidenceClient.getWebClient())
                .isEqualTo(testWebClient);
    }

    @Test
    void givenCrimeEvidenceClient_whenGetRegistrationIdIsInvoked_thenRegistrationIdIsReturned() {
        assertThat(crimeEvidenceClient.getRegistrationId())
                .isEqualTo("evidence");
    }

    @Test
    void givenCorrectParameters_whenOverloadedHeadApiResponseViaGetIsInvoked_thenCorrectMethodIsCalled()
            throws JsonProcessingException {

        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        setupValidResponseTest(response);

        crimeEvidenceClient.getApiResponseViaHEAD(
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                REP_ID
        );

        verify(crimeEvidenceClient).getApiResponseViaHEAD(any(), any(), any());
    }

    @Test
    void givenANotFoundException_whenGetApiResponseViaHeadtIsInvoked_thenTheMethodShouldReturnNull() {
        setupNotFoundTest();
        ResponseEntity response = crimeEvidenceClient.getApiResponseViaHEAD(
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                REP_ID
        );
        assertThat(response).isNull();
    }

    @Test
    void givenAnInvalidResponse_whenGetApiResponseViaHeadIsInvoked_thenAnAppropriateErrorShouldBeThrown() {
        setupInvalidResponseTest();
        assertThatThrownBy(
                () -> crimeEvidenceClient.getApiResponseViaHEAD(
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        REP_ID
                )
        ).isInstanceOf(APIClientException.class).getCause().isInstanceOf(WebClientResponseException.class);
    }

    private void setupNotFoundTest() {
        when(shortCircuitExchangeFunction.exchange(any()))
                .thenReturn(
                        Mono.just(ClientResponse
                                .create(HttpStatus.NOT_FOUND)
                                .body("Error")
                                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                .build()
                        )
                );
    }


    private void setupInvalidResponseTest() {
        when(shortCircuitExchangeFunction.exchange(any()))
                .thenReturn(
                        Mono.just(ClientResponse
                                .create(HttpStatus.FORBIDDEN)
                                .body("Invalid response")
                                .build()
                        )
                );
    }

    private <T> void setupValidResponseTest(T returnBody) throws JsonProcessingException {
        String body = OBJECT_MAPPER.writeValueAsString(returnBody);
        when(shortCircuitExchangeFunction.exchange(any()))
                .thenReturn(
                        Mono.just(ClientResponse
                                .create(HttpStatus.OK)
                                .body(body)
                                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                                .build()
                        )
                );
    }
}