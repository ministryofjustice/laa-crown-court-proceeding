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
import org.springframework.context.annotation.Import;
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
import uk.gov.justice.laa.crime.crowncourt.config.CrownCourtProceedingTestConfiguration;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.crowncourt.util.RequestBuilderUtils;

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
@Import(CrownCourtProceedingTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = CrownCourtProceedingApplication.class, webEnvironment = DEFINED_PORT)
class CrownCourtProceedingIntegrationTest {

    private static final String ERROR_MSG = "Call to service MAAT-API failed.";
    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings";

    private MockMvc mvc;
    private static MockWebServer mockMaatCourtDataApi;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeAll
    public void initialiseMockWebServer() throws IOException {
        mockMaatCourtDataApi = new MockWebServer();
        mockMaatCourtDataApi.start(9999);
    }

    @AfterAll
    protected void shutdownMockWebServer() throws IOException {
        mockMaatCourtDataApi.shutdown();
    }

    @BeforeEach
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .addFilter(springSecurityFilterChain).build();
    }

    @Test
    void givenAEmptyContent_whenProcessRepOrderIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }


    @Test
    void givenAEmptyOAuthToken_whenCreateAssessmentIsInvoked_thenFailsUnauthorizedAccess() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL, false))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenAInvalidContent_whenProcessRepOrderIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAValidContent_whenApiResponseIsError_thenProcessRepOrderIsFails() throws Exception {
        var apiProcessRepOrderRequest = TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE);
        apiProcessRepOrderRequest.setCaseType(CaseType.APPEAL_CC);

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setResponseCode(NOT_IMPLEMENTED.code()));

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, objectMapper.writeValueAsString(apiProcessRepOrderRequest), ENDPOINT_URL))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(ERROR_MSG));
    }

    @Test
    void givenAValidEitherWayCaseTypeContent_whenProcessRepOrderIsInvoked_thenSuccess() throws Exception {
        MvcResult result = mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, objectMapper.writeValueAsString(
                                TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE)), ENDPOINT_URL))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(TestModelDataBuilder.getApiProcessRepOrderResponse()));
    }

    @Test
    void givenAValidAppealCCContent_whenProcessRepOrderIsInvoked_thenSuccess() throws Exception {
        var apiProcessRepOrderRequest = TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE);
        apiProcessRepOrderRequest.setCaseType(CaseType.APPEAL_CC);
        var processRepOrderRequestJson = objectMapper.writeValueAsString(apiProcessRepOrderRequest);
        var processRepOrderResponse = TestModelDataBuilder.getApiProcessRepOrderResponse();
        processRepOrderResponse.setRepOrderDate(TestModelDataBuilder.TEST_IOJ_APPEAL_DECISION_DATE);

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setResponseCode(OK.code())
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(TestModelDataBuilder.getIOJAppealDTO()))
        );

        MvcResult result = mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, processRepOrderRequestJson, ENDPOINT_URL))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(processRepOrderResponse));
    }


    @Test
    void givenAEmptyContent_whenUpdateApplicationIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAEmptyOAuthToken_whenUpdateApplicationIsInvoked_thenFailsUnauthorizedAccess() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, "{}", ENDPOINT_URL, Boolean.FALSE))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenAInvalidContent_whenUpdateApplicationIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(
                                TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.FALSE)), ENDPOINT_URL))

                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAValidUpdateApplicationContent_whenApiResponseIsError_thenUpdateApplicationIsFails() throws Exception {

        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setResponseCode(NOT_IMPLEMENTED.code()));

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(
                                TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE)), ENDPOINT_URL))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(ERROR_MSG));
    }

    @Test
    void givenAValidContent_whenUpdateApplicationIsInvoked_thenUpdateApplicationIsSuccess() throws Exception {
        enqueueOAuthResponse();
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

        MvcResult result = mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(apiUpdateApplicationRequest), ENDPOINT_URL))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(updateApplicationResponse));
    }

    private void enqueueOAuthResponse() throws JsonProcessingException {
        Map<String, String> token = Map.of(
                "expires_in", "3600",
                "token_type", "Bearer",
                "access_token", "token"
        );
        MockResponse response = new MockResponse();
        response.setBody(objectMapper.writeValueAsString(token));

        mockMaatCourtDataApi.enqueue(response
                .setResponseCode(OK.code())
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(token))
        );
    }
}