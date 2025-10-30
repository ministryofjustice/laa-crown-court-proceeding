package uk.gov.justice.laa.crime.crowncourt.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.enums.DecisionReason;
import uk.gov.justice.laa.crime.util.RequestBuilderUtils;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;

class MagsProceedingIntegrationTest extends WiremockIntegrationTest {

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static final String REP_ORDERS_ENDPOINT_URL = "/api/internal/v1/assessment/rep-orders";
    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings/determine-mags-rep-decision";

    @BeforeEach
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .addFilter(springSecurityFilterChain)
                .build();
    }

    @Test
    void givenAEmptyContent_whenProcessRepOrderIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAEmptyOAuthToken_whenCreateAssessmentIsInvoked_thenFailsUnauthorizedAccess() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL, false))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    void givenInvalidRequest_whenDetermineMagsRepDecisionIsInvoked_thenFailsBadRequest() throws Exception {
        var apiUpdateApplicationRequest = TestModelDataBuilder.getApiDetermineMagsRepDecisionRequest(false);

        var applicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, applicationRequestJson, ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAPIClientException_whenDetermineMagsRepDecisionIsInvoked_thenFailsServerError() throws Exception {
        var apiUpdateApplicationRequest = TestModelDataBuilder.getApiDetermineMagsRepDecisionRequest(true);

        stubForOAuth();
        wiremock.stubFor(put(REP_ORDERS_ENDPOINT_URL)
                .willReturn(
                        WireMock.serverError().withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))));

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, objectMapper.writeValueAsString(apiUpdateApplicationRequest), ENDPOINT_URL))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void givenValidRequest_whenDetermineMagsRepDecisionIsInvoked_thenSucceeds() throws Exception {
        var apiUpdateApplicationRequest = TestModelDataBuilder.getApiDetermineMagsRepDecisionRequest(true);

        stubForOAuth();
        wiremock.stubFor(put(REP_ORDERS_ENDPOINT_URL)
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(objectMapper.writeValueAsString(RepOrderDTO.builder()
                                .dateModified(TestModelDataBuilder.TEST_DATE_MODIFIED)
                                .build()))));

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, objectMapper.writeValueAsString(apiUpdateApplicationRequest), ENDPOINT_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.decisionResult.decisionDate")
                        .value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.decisionResult.decisionReason").value(DecisionReason.GRANTED.getCode()))
                .andExpect(jsonPath("$.decisionResult.timestamp")
                        .value(TestModelDataBuilder.TEST_DATE_MODIFIED.toString()));
    }
}
