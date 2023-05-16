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
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.model.ApiFinancialAssessment;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class MaatAPIClientTest {

    private WebClient testWebClient;
    private final Integer REP_ID = 1234;
    private MaatAPIClient maatAPIClient;
    public static final String MOCK_URL = "mock-url";
    private final String LAA_TRANSACTION_ID = "laaTransactionId";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private ExchangeFunction shortCircuitExchangeFunction;

    @BeforeEach
    void setup() {
        testWebClient = WebClient
                .builder()
                .baseUrl("http://localhost:1234")
                .filter(ExchangeFilterFunctions.statusError(
                                HttpStatusCode::is4xxClientError, r -> {
                                    HttpStatus status = HttpStatus.valueOf(r.statusCode().value());
                                    return WebClientResponseException.create(
                                            status.value(), status.getReasonPhrase(), null, null, null
                                    );
                                }
                        )
                )
                .exchangeFunction(shortCircuitExchangeFunction)
                .build();

        maatAPIClient = Mockito.spy(new MaatAPIClient(testWebClient));
    }

    @Test
    void givenMaatAPIClient_whenGetWebClientIsInvoked_thenWebClientIsReturned() {
        assertThat(maatAPIClient.getWebClient())
                .isEqualTo(testWebClient);
    }

    @Test
    void givenMaatAPIClient_whenGetRegistrationIdIsInvoked_thenRegistrationIdIsReturned() {
        assertThat(maatAPIClient.getRegistrationId())
                .isEqualTo("maat-api");
    }

    @Test
    void givenApiClientException_whenHandleErrorIsInvoked_thenExistingErrorIsReturned() {
        String mockResponse = "MOCK ERROR RESPONSE";
        APIClientException mockException = new APIClientException(mockResponse);
        assertThat(maatAPIClient.handleError(mockException))
                .isInstanceOf(APIClientException.class).hasMessage(mockResponse);
    }

    @Test
    void givenCorrectParameters_whenOverloadedGetApiResponseViaGetIsInvoked_thenCorrectMethodIsCalled()
            throws JsonProcessingException {

        setupValidResponseTest(TestModelDataBuilder.getFinancialAssessment());

        maatAPIClient.getApiResponseViaGET(
                ApiFinancialAssessment.class,
                MOCK_URL,
                REP_ID
        );

        verify(maatAPIClient)
                .getApiResponseViaGET(
                        eq(ApiFinancialAssessment.class),
                        anyString(),
                        isNull(),
                        isNull(),
                        any()
                );
    }

    @Test
    void givenAnInvalidResponse_whenGetApiResponseIsInvoked_thenAnAppropriateErrorShouldBeThrown() {
        setupInvalidResponseTest();
        assertThatThrownBy(
                () -> maatAPIClient.getApiResponse(
                        new Object(),
                        ClientResponse.class,
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        HttpMethod.POST
                )
        ).isInstanceOf(APIClientException.class).cause().isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void givenANotFoundException_whenGetApiResponseViaGetIsInvoked_thenTheMethodShouldReturnNull() {
        setupNotFoundTest();
        ClientResponse response = maatAPIClient.getApiResponseViaGET(
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
                () -> maatAPIClient.getApiResponseViaGET(
                        ClientResponse.class,
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        REP_ID
                )
        ).isInstanceOf(APIClientException.class).cause().isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void givenCorrectParams_whenGetApiResponseViaPOSTIsInvoked_thenGetApiResponseIsCalledWithCorrectMethod() throws JsonProcessingException {
        ApiFinancialAssessment requestBody = TestModelDataBuilder.getFinancialAssessment();
        setupValidResponseTest(TestModelDataBuilder.getFinancialAssessment());
        maatAPIClient.getApiResponseViaPOST(
                requestBody,
                ApiFinancialAssessment.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID)
        );
        verify(maatAPIClient)
                .getApiResponse(
                        requestBody,
                        ApiFinancialAssessment.class,
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        HttpMethod.POST
                );
    }

    @Test
    void givenCorrectParams_whenGetApiResponseViaPUTIsInvoked_thenGetApiResponseIsCalledWithCorrectMethod() throws JsonProcessingException {
        ApiFinancialAssessment requestBody = TestModelDataBuilder.getFinancialAssessment();
        setupValidResponseTest(TestModelDataBuilder.getFinancialAssessment());
        maatAPIClient.getApiResponseViaPUT(
                requestBody,
                ApiFinancialAssessment.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID)
        );

        verify(maatAPIClient)
                .getApiResponse(
                        requestBody,
                        ApiFinancialAssessment.class,
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        HttpMethod.PUT
                );
    }

    @Test
    void givenAInvalidUrl_whenGetApiResponseViaGetIsInvoked_thenTheMethodShouldReturnNull() {
        setupNotFoundTest();
        ClientResponse response = maatAPIClient.getApiResponseViaGET(
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
                () -> maatAPIClient.getApiResponseViaGET(
                        ClientResponse.class,
                        MOCK_URL,
                        REP_ID
                )
        ).isInstanceOf(APIClientException.class).cause().isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void givenCorrectParams_whenGetApiResponseViaGETIsInvoked_thenGetApiResponseIsCalledWithCorrectMethod() throws JsonProcessingException {
        ApiProcessRepOrderResponse response = new ApiProcessRepOrderResponse();
        response.setRepOrderDecision("PASS");
        setupValidResponseTest(response);
        ApiProcessRepOrderResponse apiResponse = maatAPIClient.getApiResponseViaGET(
                ApiProcessRepOrderResponse.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                1234
        );
        verify(shortCircuitExchangeFunction, times(1)).exchange(any());
        assertThat(apiResponse.getRepOrderDecision()).isEqualTo(response.getRepOrderDecision());
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
