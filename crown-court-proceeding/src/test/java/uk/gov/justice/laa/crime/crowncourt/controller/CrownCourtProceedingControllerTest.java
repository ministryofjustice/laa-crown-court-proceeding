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
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.service.CrownCourtProceedingService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
    private static final String MEANS_ASSESSMENT_ENDPOINT_URL = "/api/internal/v1/crowncourt/actions";

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrownCourtProceedingService crownCourtProceedingService;

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
                MockMvcRequestBuilders.request(method, MEANS_ASSESSMENT_ENDPOINT_URL)
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
    void createAssessment_success() throws Exception {
        var initialMeansAssessmentRequest =
                TestModelDataBuilder.getApiCheckCrownCourtActionsRequest(IS_VALID);
        var initialMeansAssessmentRequestJson = objectMapper.writeValueAsString(initialMeansAssessmentRequest);
        var initialMeansAssessmentResponse =
                TestModelDataBuilder.getApiCheckCrownCourtActionsResponse(IS_VALID);

        when(crownCourtProceedingService.checkCrownCourtActions(any(CrownCourtsActionsRequestDTO.class)))
                .thenReturn(initialMeansAssessmentResponse);

        mvc.perform(buildRequestGivenContent(HttpMethod.POST, initialMeansAssessmentRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void createAssessment_RequestObjectFailsValidation() throws Exception {
        var createAssessmentRequest =
                TestModelDataBuilder.getApiCheckCrownCourtActionsRequest(!IS_VALID);
        var createAssessmentRequestJson = objectMapper.writeValueAsString(createAssessmentRequest);

        mvc.perform(buildRequestGivenContent(HttpMethod.POST, createAssessmentRequestJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void checkCrownCourtActions_ServerError_RequestBodyIsMissing() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, ""))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void checkCrownCourtActions_BadRequest_RequestEmptyBody() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, "{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkCrownCourtActions_Unauthorized_NoAccessToken() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.POST, "{}", false))
                .andExpect(status().isUnauthorized());
    }
}