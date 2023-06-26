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
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.service.ProceedingService;
import uk.gov.justice.laa.crime.crowncourt.util.RequestBuilderUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@Import(CrownCourtProceedingTestConfiguration.class)
@SpringBootTest(classes = {CrownCourtProceedingApplication.class}, webEnvironment = DEFINED_PORT)
class CrownCourtProceedingControllerTest {

    private static final boolean IS_VALID = true;
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

    @Test
    void processRepOrder_Success() throws Exception {
        var apiProcessRepOrderRequest =
                TestModelDataBuilder.getApiProcessRepOrderRequest(IS_VALID);
        var processRepOrderRequestJson = objectMapper.writeValueAsString(apiProcessRepOrderRequest);
        var processRepOrderResponse =
                TestModelDataBuilder.getApiProcessRepOrderResponse();

        when(proceedingService.processRepOrder(any(CrownCourtDTO.class)))
                .thenReturn(processRepOrderResponse);

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, processRepOrderRequestJson, ENDPOINT_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void processRepOrder_RequestObjectFailsValidation() throws Exception {
        var processRepOrderRequest =
                TestModelDataBuilder.getApiProcessRepOrderRequest(!IS_VALID);
        var processRepOrderRequestJson = objectMapper.writeValueAsString(processRepOrderRequest);

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, processRepOrderRequestJson, ENDPOINT_URL))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void processRepOrder_ServerError_RequestBodyIsMissing() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "", ENDPOINT_URL))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void processRepOrder_BadRequest_RequestEmptyBody() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processRepOrder_Unauthorized_NoAccessToken() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL, false))
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

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, updateApplicationRequestJson, ENDPOINT_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void updateApplication_RequestObjectFailsValidation() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiUpdateApplicationRequest(!IS_VALID);
        var updateApplicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, updateApplicationRequestJson, ENDPOINT_URL))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateApplication_ServerError_RequestBodyIsMissing() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, "", ENDPOINT_URL))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateApplication_BadRequest_RequestEmptyBody() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateApplication_Unauthorized_NoAccessToken() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.PUT, "{}", ENDPOINT_URL, false))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenAValidInput_whenUpdateIsInvoked_thenSuccess() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiUpdateApplicationRequest(IS_VALID);
        var updateRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);
        var updateResponse = TestModelDataBuilder.getApiUpdateCrownCourtOutcomeResponse();
        when(proceedingService.update(any(CrownCourtDTO.class)))
                .thenReturn(updateResponse);

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, updateRequestJson, ENDPOINT_URL + "/update-crown-court"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void givenAValidInput_whenUpdateIsInvoked_RequestObjectFailsValidation() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiUpdateApplicationRequest(!IS_VALID);
        var updateApplicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, updateApplicationRequestJson, ENDPOINT_URL + "/update-crown-court"))
                .andExpect(status().is4xxClientError());
    }
}