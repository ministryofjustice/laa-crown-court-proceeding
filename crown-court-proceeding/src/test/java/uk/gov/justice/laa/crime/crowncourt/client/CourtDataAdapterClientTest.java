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
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.model.ApiFinancialAssessment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class CourtDataAdapterClientTest {

    private WebClient testWebClient;
    private final Integer REP_ID = 1234;
    private CourtDataAdapterClient courtDataAdapterClient;
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
                                HttpStatus::is4xxClientError,
                                r -> WebClientResponseException.create(
                                        r.rawStatusCode(), r.statusCode().getReasonPhrase(), null, null, null
                                )
                        )
                )
                .exchangeFunction(shortCircuitExchangeFunction)
                .build();

        courtDataAdapterClient = Mockito.spy(new CourtDataAdapterClient(testWebClient));
    }

    @Test
    void givenMaatAPIClient_whenGetWebClientIsInvoked_thenWebClientIsReturned() {
        assertThat(courtDataAdapterClient.getWebClient())
                .isEqualTo(testWebClient);
    }

    @Test
    void givenMaatAPIClient_whenGetRegistrationIdIsInvoked_thenRegistrationIdIsReturned() {
        assertThat(courtDataAdapterClient.getRegistrationId())
                .isEqualTo("cda");
    }

    @Test
    void givenApiClientException_whenHandleErrorIsInvoked_thenExistingErrorIsReturned() {
        String mockResponse = "MOCK ERROR RESPONSE";
        APIClientException mockException = new APIClientException(mockResponse);
        assertThat(courtDataAdapterClient.handleError(mockException))
                .isInstanceOf(APIClientException.class).hasMessage(mockResponse);
    }

    @Test
    void givenCorrectParameters_whenOverloadedGetApiResponseViaGetIsInvoked_thenCorrectMethodIsCalled()
            throws JsonProcessingException {

        setupValidResponseTest(TestModelDataBuilder.getFinancialAssessment());

        courtDataAdapterClient.getApiResponseViaGET(
                ApiFinancialAssessment.class,
                MOCK_URL,
                REP_ID
        );

        verify(courtDataAdapterClient)
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
                () -> courtDataAdapterClient.getApiResponse(
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
        ClientResponse response = courtDataAdapterClient.getApiResponseViaGET(
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
                () -> courtDataAdapterClient.getApiResponseViaGET(
                        ClientResponse.class,
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        REP_ID
                )
        ).isInstanceOf(APIClientException.class).getCause().isInstanceOf(WebClientResponseException.class);
    }

    @Test
    void givenCorrectParams_whenGetApiResponseViaPOSTIsInvoked_thenGetApiResponseIsCalledWithCorrectMethod() throws JsonProcessingException {
        ApiFinancialAssessment requestBody = TestModelDataBuilder.getFinancialAssessment();
        setupValidResponseTest(TestModelDataBuilder.getFinancialAssessment());
        courtDataAdapterClient.getApiResponseViaPOST(
                requestBody,
                ApiFinancialAssessment.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID)
        );
        verify(courtDataAdapterClient)
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

        courtDataAdapterClient.getApiResponseViaPUT(
                requestBody,
                ApiFinancialAssessment.class,
                MOCK_URL,
                Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID)
        );

        verify(courtDataAdapterClient)
                .getApiResponse(
                        requestBody,
                        ApiFinancialAssessment.class,
                        MOCK_URL,
                        Map.of("LAA_TRANSACTION_ID", LAA_TRANSACTION_ID),
                        HttpMethod.PUT
                );
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
