package uk.gov.justice.laa.crime.crowncourt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
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
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.exception.APIClientException;
import uk.gov.justice.laa.crime.crowncourt.service.ProceedingService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(CrownCourtProceedingTestConfiguration.class)
@SpringBootTest(classes = {CrownCourtProceedingApplication.class}, webEnvironment = DEFINED_PORT)
@DirtiesContext
class CrownCourtProceedingControllerTest {

    private static final boolean IS_VALID = true;
    private static final String CLIENT_SECRET = "secret";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String CLIENT_ID = "test-client";
    private static final String SCOPE_READ_WRITE = "READ_WRITE";
    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings";

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProceedingService proceedingService;

    @BeforeEach
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .addFilter(springSecurityFilterChain).build();
    }

    private MockHttpServletRequestBuilder buildRequestGivenContent(HttpMethod method, String content) throws Exception {
        return buildRequestGivenContent(method, content, true);
    }

    private MockHttpServletRequestBuilder buildRequestGivenContent(HttpMethod method, String content, boolean withAuth) throws Exception {
        MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.request(method, ENDPOINT_URL)
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

    @Test
    void processRepOrder_Success() throws Exception {
        var apiProcessRepOrderRequest =
                TestModelDataBuilder.getApiProcessRepOrderRequest(IS_VALID);
        var processRepOrderRequestJson = objectMapper.writeValueAsString(apiProcessRepOrderRequest);
        var processRepOrderResponse =
                TestModelDataBuilder.getApiProcessRepOrderResponse();

        when(proceedingService.processRepOrder(any(CrownCourtDTO.class)))
                .thenReturn(processRepOrderResponse);

        mvc.perform(buildRequestGivenContent(HttpMethod.POST, processRepOrderRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void processRepOrder_RequestObjectFailsValidation() throws Exception {
        var processRepOrderRequest =
                TestModelDataBuilder.getApiProcessRepOrderRequest(!IS_VALID);
        var processRepOrderRequestJson = objectMapper.writeValueAsString(processRepOrderRequest);

        mvc.perform(buildRequestGivenContent(HttpMethod.POST, processRepOrderRequestJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void processRepOrder_ServerError_RequestBodyIsMissing() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, ""))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void processRepOrder_BadRequest_RequestEmptyBody() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, "{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processRepOrder_Unauthorized_NoAccessToken() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, "{}", false))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateApplication_Success() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiUpdateApplicationRequest(IS_VALID);
        var updateApplicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);
        var updateApplicationResponse = TestModelDataBuilder.getApiUpdateApplicationResponse();
        when(proceedingService.updateApplication(any(CrownCourtDTO.class)))
                .thenReturn(updateApplicationResponse);

        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, updateApplicationRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void updateApplication_RequestObjectFailsValidation() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiUpdateApplicationRequest(!IS_VALID);
        var updateApplicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);

        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, updateApplicationRequestJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateApplication_ServerError_RequestBodyIsMissing() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, ""))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateApplication_BadRequest_RequestEmptyBody() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, "{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateApplication_Unauthorized_NoAccessToken() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, "{}", false))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenAValidContent_whenGraphQLQueryIsInvoked_thenSuccess() throws Exception {
        when(proceedingService.graphQLQuery()).thenReturn(TestModelDataBuilder.getGraphQLRepOrderDTO());
        MvcResult result = mvc.perform(buildRequestGivenContent(HttpMethod.POST, ENDPOINT_URL + "/graphql", "{}", Boolean.TRUE))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(TestModelDataBuilder.getGraphQLRepOrderDTO()));
    }

    @Test
    void givenAValidContent_whenApiResponseIsError_thenGraphQLIsFails() throws Exception {

        doThrow(new APIClientException()).when(proceedingService).graphQLQuery();
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, ENDPOINT_URL + "/graphql", "{}", Boolean.TRUE))
                .andExpect(status().is5xxServerError());
    }
}