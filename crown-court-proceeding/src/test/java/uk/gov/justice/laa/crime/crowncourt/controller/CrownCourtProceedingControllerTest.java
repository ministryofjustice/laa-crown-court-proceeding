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
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtApplicationRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.service.CrownCourtProceedingService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
    private static final String CROWN_COURT_ACTIONS_ENDPOINT_URL = "/api/internal/v1/crowncourtproceeding/actions";

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
                MockMvcRequestBuilders.request(method, CROWN_COURT_ACTIONS_ENDPOINT_URL)
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
    void checkCrownCourtActions_Success() throws Exception {
        var apiCheckCrownCourtActionsRequest =
                TestModelDataBuilder.getApiCheckCrownCourtActionsRequest(IS_VALID);
        var checkCrownCourtActionsRequestJson = objectMapper.writeValueAsString(apiCheckCrownCourtActionsRequest);
        var checkCrownCourtActionsResponse =
                TestModelDataBuilder.getApiCheckCrownCourtActionsResponse();

        when(crownCourtProceedingService.checkCrownCourtActions(any(CrownCourtActionsRequestDTO.class)))
                .thenReturn(checkCrownCourtActionsResponse);

        mvc.perform(buildRequestGivenContent(HttpMethod.POST, checkCrownCourtActionsRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void checkCrownCourtActions_RequestObjectFailsValidation() throws Exception {
        var checkCrownCourtActionsRequest =
                TestModelDataBuilder.getApiCheckCrownCourtActionsRequest(!IS_VALID);
        var checkCrownCourtActionsRequestJson = objectMapper.writeValueAsString(checkCrownCourtActionsRequest);

        mvc.perform(buildRequestGivenContent(HttpMethod.POST, checkCrownCourtActionsRequestJson))
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
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCrownCourtActions_Success() throws Exception {
        var apiUpdateCrownCourtApplicationRequest =
                TestModelDataBuilder.getApiUpdateCrownCourtApplicationRequest(IS_VALID);
        var updateCCApplicationRequestJson = objectMapper.writeValueAsString(apiUpdateCrownCourtApplicationRequest);
        doNothing().when(crownCourtProceedingService).updateCrownCourtApplication(any(CrownCourtApplicationRequestDTO.class));

        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, updateCCApplicationRequestJson))
                .andExpect(status().isOk());
    }

    @Test
    void updateCrownCourtActions_RequestObjectFailsValidation() throws Exception {
        var apiUpdateCCApplicationRequest =
                TestModelDataBuilder.getApiUpdateCrownCourtApplicationRequest(!IS_VALID);
        var updateCCApplicationRequestJson = objectMapper.writeValueAsString(apiUpdateCCApplicationRequest);

        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, updateCCApplicationRequestJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateCrownCourtActions_ServerError_RequestBodyIsMissing() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, ""))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateCrownCourtActions_BadRequest_RequestEmptyBody() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, "{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCrownCourtActions_Unauthorized_NoAccessToken() throws Exception {
        mvc.perform(buildRequestGivenContent(HttpMethod.PUT, "{}", false))
                .andExpect(status().isForbidden());
    }
}