package uk.gov.justice.laa.crime.crowncourt.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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
import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DirtiesContext
@RunWith(SpringRunner.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CrownCourtProceedingApplication.class, webEnvironment = DEFINED_PORT)
class CrownCourtProceedingIntegrationTest {

    private static final String CLIENT_SECRET = "secret";
    private static final String CLIENT_ID = "test-client";
    private static final String SCOPE_READ_WRITE = "READ_WRITE";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String ERROR_MSG = "Call to service MAAT-API failed.";
    private static final String CCP_ENDPOINT_URL = "/api/internal/v1/proceedings";

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockWebServer mockMaatCourtDataApi;


    @BeforeAll
    public void initialiseMockWebServer() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .addFilter(springSecurityFilterChain).build();
    }

    @AfterEach
    protected void shutdownMockWebServer() throws IOException {
        mockMaatCourtDataApi.shutdown();
    }

    @BeforeEach
    public void setup() throws IOException {
        mockMaatCourtDataApi = new MockWebServer();
        mockMaatCourtDataApi.start(9999);

        final Dispatcher dispatcher = new QueueDispatcher() {
            @NotNull @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                if ("/oauth2/token".equals(request.getPath())) {
                    return getOauthResponse();
                }

                var requestLine = request.getRequestLine();
                if ("GET /favicon.ico HTTP/1.1".equals(requestLine)) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
                }

                return getResponseQueue().take();
            }
        };
        mockMaatCourtDataApi.setDispatcher(dispatcher);
    }

    private MockResponse getOauthResponse() {
        Map<String, Object> token = Map.of(
                "expires_in", 3600,
                "token_type", "Bearer",
                "access_token", UUID.randomUUID()
        );
        String responseBody;
        MockResponse response = new MockResponse();
        response.setResponseCode(OK.code());
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON);

        try {
            responseBody = objectMapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response.setBody(responseBody);
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

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setResponseCode(NOT_IMPLEMENTED.code()));

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

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setResponseCode(OK.code())
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(TestModelDataBuilder.getIOJAppealDTO()))
        );

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

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setResponseCode(NOT_IMPLEMENTED.code()));

        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, objectMapper.writeValueAsString(
                        TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE))))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(ERROR_MSG));
    }

    @Test
    void givenAValidContent_whenUpdateApplicationIsInvoked_thenUpdateApplicationIsSuccess() throws Exception {
        var updateApplicationResponse = TestModelDataBuilder.getApiUpdateApplicationResponse();
        ApiUpdateApplicationRequest apiUpdateApplicationRequest = TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE);
        apiUpdateApplicationRequest.setCaseType(CaseType.APPEAL_CC);

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setResponseCode(OK.code())
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(TestModelDataBuilder.getIOJAppealDTO()))
        );

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setResponseCode(OK.code())
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(TestModelDataBuilder.getRepOrderDTO()))
        );

        MvcResult result = mvc.perform(buildRequestGivenContent(HttpMethod.PUT, objectMapper.writeValueAsString(apiUpdateApplicationRequest)))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(updateApplicationResponse));
    }

    @Test
    void givenAValidContent_whenGraphQLQueryIsInvoked_thenSuccess() throws Exception {

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setResponseCode(OK.code())
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(TestModelDataBuilder.getGraphQLRepOrderDTO()))
        );

        MvcResult result = mvc.perform(buildRequestGivenContent(HttpMethod.POST, CCP_ENDPOINT_URL + "/graphql",
                        "{}", Boolean.TRUE))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(TestModelDataBuilder.getGraphQLRepOrderDTO()));

    }

    @Test
    void givenAValidContent_whenApiResponseIsError_thenGraphQLIsFails() throws Exception {

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setResponseCode(NOT_IMPLEMENTED.code()));

        mvc.perform(buildRequestGivenContent(HttpMethod.POST,
                        CCP_ENDPOINT_URL + "/graphql", "{}", Boolean.TRUE))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(ERROR_MSG));
    }
}