package uk.gov.justice.laa.crime.crowncourt.client;

import exception.APIClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class MaatCourtDataClientTest {

    public static final String MOCK_URL = "mock-url";
    private final Integer REP_ID = 1234;
    private final String LAA_TRANSACTION_ID = "laaTransactionId";
    private MaatCourtDataClient maatCourtDataClient;
    @Mock
    private ExchangeFunction shortCircuitExchangeFunction;

    @BeforeEach
    void setup() {
        WebClient testWebClient = WebClient
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

        maatCourtDataClient = Mockito.spy(new MaatCourtDataClient(testWebClient));
    }

    @Test
    void givenAnInvalidResponse_whenGetApiResponseIsInvoked_thenAnAppropriateErrorShouldBeThrown() {
        setupInvalidResponseTest();
        assertThatThrownBy(
                () -> maatCourtDataClient.getApiResponse(
                        new Object(),
                        ClientResponse.class,
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        HttpMethod.POST
                )
        ).isInstanceOf(APIClientException.class).getCause().isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void givenANotFoundException_whenGetApiResponseViaGetIsInvoked_thenTheMethodShouldReturnNull() {
        setupNotFoundTest();
        ClientResponse response = maatCourtDataClient.getApiResponseViaGET(
                ClientResponse.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                REP_ID
        );
        assertThat(response).isNull();
    }

    @Test
    void givenAnInvalidResponse_whenGetApiResponseViaGetIsInvoked_thenAnAppropriateErrorShouldBeThrown() {
        setupInvalidResponseTest();
        assertThatThrownBy(
                () -> maatCourtDataClient.getApiResponseViaGET(
                        ClientResponse.class,
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
                                .create(HttpStatus.OK)
                                .body("Invalid response")
                                .build()
                        )
                );
    }
}
