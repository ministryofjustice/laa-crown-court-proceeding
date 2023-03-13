package uk.gov.justice.laa.crime.crowncourt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.client.MaatAPIClient;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = {
        CrownCourtProceedingApplication.class}, webEnvironment = DEFINED_PORT)
@RunWith(SpringRunner.class)
@DirtiesContext
class CrownCourtProceedingIntegrationTest {

    private static final String CLIENT_SECRET = "secret";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String CLIENT_ID = "test-client";
    private static final String SCOPE_READ_WRITE = "READ_WRITE";
    private static final String CCP_ENDPOINT_URL = "/api/internal/v1/proceedings";

    private static final String ERROR_MSG = "Call to service failed.";

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @MockBean
    private WebClient webClient;
    @MockBean
    private MaatAPIClient maatAPIClient;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @MockBean
    private ExchangeFunction shortCircuitExchangeFunction;


    @BeforeEach
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).addFilter(springSecurityFilterChain).build();
    }

    private MockHttpServletRequestBuilder buildRequestGivenContent(HttpMethod method, String content) throws Exception {
        return buildRequestGivenContent(method, content, true);
    }

    private MockHttpServletRequestBuilder buildRequestGivenContent(HttpMethod method, String content, boolean withAuth) throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.request(method, CCP_ENDPOINT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content);
        if (withAuth) {
            final String accessToken = obtainAccessToken();
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }
        return requestBuilder;
    }

    private MockHttpServletRequestBuilder buildRequestGivenContent(HttpMethod method, String endpointUrl, String content,
                                                                   boolean withAuth) throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.request(method, endpointUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content);
        if (withAuth) {
            final String accessToken = obtainAccessToken();
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }
        return requestBuilder;
    }

    private MockHttpServletRequestBuilder buildRequestForGet(HttpMethod method, String url, boolean withAuth) throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.request(method, url)
                        .contentType(MediaType.APPLICATION_JSON);
        if (withAuth) {
            final String accessToken = obtainAccessToken();
            requestBuilder.header("Authorization", "Bearer " + accessToken);
            requestBuilder.header("Laa-Transaction-Id", TestModelDataBuilder.MEANS_ASSESSMENT_TRANSACTION_ID);
        }
        return requestBuilder;
    }

    private String obtainAccessToken() throws Exception {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", CLIENT_CREDENTIALS);
        params.add("scope", SCOPE_READ_WRITE);

        ResultActions result = mvc.perform(post("/oauth2/token")
                        .params(params)
                        .with(httpBasic(CLIENT_ID, CLIENT_SECRET)))
                .andExpect(status().isOk());
        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("access_token").toString();
    }

    @Test
    void givenAEmptyContent_whenProcessRepOrderIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, "{}"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void givenAEmptyOAuthToken_whenCreateAssessmentIsInvoked_thenFailsUnauthorizedAccess() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, "{}", Boolean.FALSE))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenAInvalidContent_whenProcessRepOrderIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, objectMapper.writeValueAsString(
                        TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.FALSE))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAValidContent_whenApiResponseIsError_thenProcessRepOrderIsFails() throws Exception {
        var apiProcessRepOrderRequest = TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE);
        apiProcessRepOrderRequest.setCaseType(CaseType.APPEAL_CC);

        doThrow(new APIClientException(ERROR_MSG)).when(maatAPIClient).getApiResponseViaGET(any(), any(), any(), any());

        mvc.perform(buildRequestGivenContent(HttpMethod.POST, objectMapper.writeValueAsString(apiProcessRepOrderRequest)))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(ERROR_MSG));
    }

    @Test
    void givenAValidEitherWayCaseTypeContent_whenProcessRepOrderIsInvoked_thenSuccess() throws Exception {
        MvcResult result = mvc.perform(buildRequestGivenContent(HttpMethod.POST, objectMapper.writeValueAsString(
                        TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE))))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(TestModelDataBuilder.getApiProcessRepOrderResponse()));
    }

    @Test
    void givenAValidAppealCCContent_whenProcessRepOrderIsInvoked_thenSuccess() throws Exception {
        var apiProcessRepOrderRequest = TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE);
        apiProcessRepOrderRequest.setCaseType(CaseType.APPEAL_CC);
        var processRepOrderResponse = TestModelDataBuilder.getApiProcessRepOrderResponse();
        processRepOrderResponse.setRepOrderDate(TestModelDataBuilder.TEST_IOJ_APPEAL_DECISION_DATE);

        doReturn(TestModelDataBuilder.getIOJAppealDTO()).when(maatAPIClient).getApiResponseViaGET(any(), any(), any(), any());

        MvcResult result = mvc.perform(buildRequestGivenContent(HttpMethod.POST, objectMapper.writeValueAsString(apiProcessRepOrderRequest)))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(processRepOrderResponse));
    }


    @Test
    void givenAEmptyContent_whenUpdateApplicationIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, "{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAEmptyOAuthToken_whenUpdateApplicationIsInvoked_thenFailsUnauthorizedAccess() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, "{}", Boolean.FALSE))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenAInvalidContent_whenUpdateApplicationIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, objectMapper.writeValueAsString(
                        TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.FALSE))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAValidUpdateApplicationContent_whenApiResponseIsError_thenUpdateApplicationIsFails() throws Exception {

        doThrow(new APIClientException(ERROR_MSG)).when(maatAPIClient).getApiResponseViaPUT(any(), any(), any(), any());

        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, objectMapper.writeValueAsString(
                        TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE))))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(ERROR_MSG));
    }

    @Test
    void givenAValidContent_whenUpdateApplicationIsInvoked_thenUpdateApplicationIsSuccess() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, objectMapper.writeValueAsString(
                        TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE))))
                .andExpect(status().isOk());
    }

    @Test
    void givenAValidContent_whenGraphQLQueryIsInvoked_thenSuccess() throws Exception {

        doReturn(TestModelDataBuilder.getGraphQLRepOrderDTO()).when(maatAPIClient).getGraphQLApiResponse(any(), any(), any());
        MvcResult result = mvc.perform(buildRequestGivenContent(HttpMethod.POST, CCP_ENDPOINT_URL + "/graphql",
                        "{}", Boolean.TRUE))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(TestModelDataBuilder.getGraphQLRepOrderDTO()));

    }

    @Test
    void givenAValidContent_whenApiResponseIsError_thenGraphQLIsFails() throws Exception {

        doThrow(new APIClientException(ERROR_MSG)).when(maatAPIClient).getGraphQLApiResponse(any(), any(), any());
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, CCP_ENDPOINT_URL + "/graphql", "{}", Boolean.TRUE))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(ERROR_MSG));

    }
}