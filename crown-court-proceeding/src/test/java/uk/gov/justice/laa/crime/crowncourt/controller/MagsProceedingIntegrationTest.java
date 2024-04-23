package uk.gov.justice.laa.crime.crowncourt.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.justice.laa.crime.crowncourt.config.IntegrationTestConfiguration;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.model.common.ApiFinancialAssessment;
import uk.gov.justice.laa.crime.crowncourt.model.common.ApiIOJSummary;
import uk.gov.justice.laa.crime.crowncourt.model.request.ApiDetermineMagsRepDecisionRequest;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.DecisionReason;
import uk.gov.justice.laa.crime.util.RequestBuilderUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext
@AutoConfigureObservability
@AutoConfigureWireMock(port = 9998)
@Import(IntegrationTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = IntegrationTestConfiguration.class, webEnvironment = DEFINED_PORT)
class MagsProceedingIntegrationTest {

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private WireMockServer wiremock;

    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings/determine-mags-rep-decision";

    @AfterEach
    void clean() {
        wiremock.resetAll();
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
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void givenInvalidRequest_whenDetermineMagsRepDecisionIsInvoked_thenFailsBadRequest() throws Exception {
        var apiUpdateApplicationRequest =
                new ApiDetermineMagsRepDecisionRequest()
                        .withRepId(null)
                        .withCaseType(CaseType.INDICTABLE)
                        .withUserSession(TestModelDataBuilder.getApiUserSession(true))
                        .withIojAppeal(new ApiIOJSummary().withIojResult("PASS"))
                        .withPassportAssessment(TestModelDataBuilder.getPassportAssessment())
                        .withFinancialAssessment(new ApiFinancialAssessment().withInitResult("PASS"));

        var applicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, applicationRequestJson, ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAPIClientException_whenDetermineMagsRepDecisionIsInvoked_thenFailsServerError() throws Exception {
        var apiUpdateApplicationRequest =
                new ApiDetermineMagsRepDecisionRequest()
                        .withCaseType(CaseType.INDICTABLE)
                        .withRepId(TestModelDataBuilder.TEST_REP_ID)
                        .withUserSession(TestModelDataBuilder.getApiUserSession(true))
                        .withIojAppeal(new ApiIOJSummary().withIojResult("PASS"))
                        .withPassportAssessment(TestModelDataBuilder.getPassportAssessment())
                        .withFinancialAssessment(new ApiFinancialAssessment().withInitResult("PASS"));

        stubForOAuth();
        wiremock.stubFor(put("/api/internal/v1/assessment/rep-orders")
                                 .willReturn(
                                         WireMock.serverError()
                                                 .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                 )
        );

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, objectMapper.writeValueAsString(apiUpdateApplicationRequest), ENDPOINT_URL))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void givenValidRequest_whenDetermineMagsRepDecisionIsInvoked_thenSucceeds() throws Exception {
        var apiUpdateApplicationRequest =
                new ApiDetermineMagsRepDecisionRequest()
                        .withCaseType(CaseType.INDICTABLE)
                        .withRepId(TestModelDataBuilder.TEST_REP_ID)
                        .withUserSession(TestModelDataBuilder.getApiUserSession(true))
                        .withIojAppeal(new ApiIOJSummary().withIojResult("PASS"))
                        .withPassportAssessment(TestModelDataBuilder.getPassportAssessment())
                        .withFinancialAssessment(new ApiFinancialAssessment().withInitResult("PASS"));

        stubForOAuth();
        wiremock.stubFor(put("/api/internal/v1/assessment/rep-orders")
                                 .willReturn(
                                         WireMock.ok()
                                                 .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                                 .withBody(objectMapper.writeValueAsString(
                                                         RepOrderDTO.builder()
                                                                 .dateModified(TestModelDataBuilder.TEST_DATE_MODIFIED)
                                                                 .build()
                                                 ))
                                 )
        );

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, objectMapper.writeValueAsString(apiUpdateApplicationRequest), ENDPOINT_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.decisionResult.decisionDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.decisionResult.decisionReason").value(DecisionReason.GRANTED.getCode()))
                .andExpect(jsonPath("$.decisionResult.timestamp").value(
                        TestModelDataBuilder.TEST_DATE_MODIFIED.toString()));
    }

    private void stubForOAuth() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> token = Map.of(
                "expires_in", 3600,
                "token_type", "Bearer",
                "access_token", UUID.randomUUID()
        );

        wiremock.stubFor(
                post("/oauth2/token").willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(mapper.writeValueAsString(token))
                )
        );
    }
}
