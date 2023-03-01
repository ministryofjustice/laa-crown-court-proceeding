package uk.gov.justice.laa.crime.crowncourt.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class MaatCourtDataClientTest {

    public static final String MOCK_URL = "mock-url";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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

    @Test
    void givenAInvalidUrl_whenGetApiResponseViaGetIsInvoked_thenTheMethodShouldReturnNull() {
        setupNotFoundTest();
        ClientResponse response = maatCourtDataClient.getApiResponseViaGET(
                ClientResponse.class,
                MOCK_URL,
                REP_ID
        );
        assertThat(response).isNull();
    }

    @Test
    void givenAInvalidResponse_whenGetApiResponseViaGetIsInvoked_thenAnAppropriateErrorShouldBeThrown() {
        setupInvalidResponseTest();
        assertThatThrownBy(
                () -> maatCourtDataClient.getApiResponseViaGET(
                        ClientResponse.class,
                        MOCK_URL,
                        REP_ID
                )
        ).isInstanceOf(APIClientException.class).getCause().isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void givenCorrectParams_whenGetApiResponseViaPOSTIsInvoked_thenGetApiResponseIsCalledWithCorrectMethod() throws JsonProcessingException {
        ApiProcessRepOrderRequest requestBody = new ApiProcessRepOrderRequest();
        setupValidResponseTest(new ApiProcessRepOrderResponse());
        maatCourtDataClient.getApiResponseViaPOST(
                requestBody,
                ApiProcessRepOrderResponse.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID)
        );
        verify(maatCourtDataClient)
                .getApiResponse(
                        requestBody,
                        ApiProcessRepOrderResponse.class,
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        HttpMethod.POST
                );
    }

    @Test
    void givenCorrectParams_whenGetApiResponseViaPUTIsInvoked_thenGetApiResponseIsCalledWithCorrectMethod() throws JsonProcessingException {
        ApiProcessRepOrderRequest requestBody = new ApiProcessRepOrderRequest();
        setupValidResponseTest(new ApiProcessRepOrderResponse());
        maatCourtDataClient.getApiResponseViaPUT(
                requestBody,
                ApiProcessRepOrderResponse.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID)
        );
        verify(maatCourtDataClient)
                .getApiResponse(
                        requestBody,
                        ApiProcessRepOrderResponse.class,
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        HttpMethod.PUT
                );
    }

    @Test
    void givenCorrectParams_whenGetApiResponseViaGETIsInvoked_thenGetApiResponseIsCalledWithCorrectMethod() throws JsonProcessingException {
        ApiProcessRepOrderResponse response = new ApiProcessRepOrderResponse();
        response.setRepOrderDecision("PASS");
        setupValidResponseTest(response);
        ApiProcessRepOrderResponse apiResponse = maatCourtDataClient.getApiResponseViaGET(
                ApiProcessRepOrderResponse.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                1234
        );
        verify(shortCircuitExchangeFunction, times(1)).exchange(any());
        assertThat(apiResponse.getRepOrderDecision()).isEqualTo(response.getRepOrderDecision());
    }

    @Test
    void givenCorrectParams_whenGetGraphQLApiResponseIsInvoked_thenGetApiResponseIsCalledWithCorrectMethod()
            throws JsonProcessingException {
        RepOrderDTO responseBody = new RepOrderDTO();
        responseBody.setId(1234);
        setupValidResponseTest(responseBody);
        RepOrderDTO apiRes = maatCourtDataClient.getGraphQLApiResponse(
                RepOrderDTO.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID)
        );
        assertThat(apiRes.getId()).isEqualTo(responseBody.getId());
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
