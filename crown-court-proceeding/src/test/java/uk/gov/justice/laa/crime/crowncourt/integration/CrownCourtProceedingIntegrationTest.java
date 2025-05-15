package uk.gov.justice.laa.crime.crowncourt.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder.TEST_REP_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateCrownCourtRequest;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.tracing.TraceIdHandler;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.enums.EvidenceFeeLevel;
import uk.gov.justice.laa.crime.util.RequestBuilderUtils;

class CrownCourtProceedingIntegrationTest extends WiremockIntegrationTest {

    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TraceIdHandler traceIdHandler;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private static final boolean IS_VALID = true;
    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings";
    private static final String UPDATE_CC_URL = "/update-crown-court";
    private static final String CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME =
            "Cannot have Crown Court outcome without Mags Court outcome";

    @BeforeEach
    void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .addFilter(springSecurityFilterChain).build();
    }

    @Test
    void givenEmptyContent_whenProcessRepOrderIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenEmptyOAuthToken_whenCreateAssessmentIsInvoked_thenFailsUnauthorizedAccess() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL, false))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    void givenInvalidContent_whenProcessRepOrderIsInvoked_thenFailsBadRequest() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiProcessRepOrderRequest(!IS_VALID);
        var applicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, applicationRequestJson, ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidContent_whenApiResponseIsError_thenProcessRepOrderIsFails() throws Exception {
        var stubPath = "/api/internal/v1/assessment/ioj-appeal/repId/" + TEST_REP_ID + "/current-passed";
        var apiProcessRepOrderRequest = TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE);
        apiProcessRepOrderRequest.setCaseType(CaseType.APPEAL_CC);

        stubForOAuth();
        wiremock.stubFor(get(urlEqualTo(stubPath))
                .willReturn(
                        WireMock.serverError()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                )
        );

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, objectMapper.writeValueAsString(apiProcessRepOrderRequest), ENDPOINT_URL))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("500 Internal Server Error")))
                .andExpect(jsonPath("$.message", containsString(stubPath)));
    }

    @Test
    void givenValidEitherWayCaseTypeContent_whenProcessRepOrderIsInvoked_thenSuccess() throws Exception {
        MvcResult result = mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, objectMapper.writeValueAsString(
                                TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE)), ENDPOINT_URL))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isEqualTo(
                objectMapper.writeValueAsString(TestModelDataBuilder.getApiProcessRepOrderResponse()));
    }

    @Test
    void givenValidAppealCCContent_whenProcessRepOrderIsInvoked_thenSuccess() throws Exception {
        var apiProcessRepOrderRequest = TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE);
        apiProcessRepOrderRequest.setCaseType(CaseType.APPEAL_CC);
        var processRepOrderRequestJson = objectMapper.writeValueAsString(apiProcessRepOrderRequest);
        var processRepOrderResponse = TestModelDataBuilder.getApiProcessRepOrderResponse();
        processRepOrderResponse.setRepOrderDate(TestModelDataBuilder.TEST_IOJ_APPEAL_DECISION_DATE);
        stubForOAuth();
        stubForIoJAppeal();
        MvcResult result = mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, processRepOrderRequestJson, ENDPOINT_URL))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(processRepOrderResponse));
    }


    @Test
    void givenEmptyContent_whenUpdateApplicationIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void givenEmptyOAuthToken_whenUpdateApplicationIsInvoked_thenFailsUnauthorizedAccess() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, "{}", ENDPOINT_URL, Boolean.FALSE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenInvalidContent_whenUpdateApplicationIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(
                                TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.FALSE)), ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAValidUpdateApplicationContent_whenApiResponseIsError_thenUpdateApplicationIsFails() throws Exception {
        var stubPath = "/api/internal/v1/assessment/rep-orders";

        stubForOAuth();
        wiremock.stubFor(put(urlEqualTo(stubPath))
                .willReturn(
                        WireMock.serverError()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                )
        );

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(
                                TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE)), ENDPOINT_URL))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message", containsString("500 Internal Server Error")))
                .andExpect(jsonPath("$.message", containsString(stubPath)));
    }

    @Test
    void givenValidContent_whenUpdateApplicationIsInvoked_thenUpdateApplicationIsSuccess() throws Exception {
        var updateApplicationResponse = TestModelDataBuilder.getApiUpdateApplicationResponse();
        ApiUpdateApplicationRequest
                apiUpdateApplicationRequest = TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE);
        apiUpdateApplicationRequest.setCaseType(CaseType.APPEAL_CC);
        stubForOAuth();
        stubForIoJAppeal();

        wiremock.stubFor(put("/api/internal/v1/assessment/rep-orders")
                .willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(objectMapper.writeValueAsString(TestModelDataBuilder.getRepOrderDTO()))
                )
        );

        MvcResult result = mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(apiUpdateApplicationRequest), ENDPOINT_URL))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString())
                .isEqualTo(objectMapper.writeValueAsString(updateApplicationResponse));
    }


    @Test
    void givenAEmptyOAuthToken_whenUpdateIsInvoked_thenFailsUnauthorizedAccess() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, "{}", ENDPOINT_URL + UPDATE_CC_URL, Boolean.FALSE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenIndictableCaseTypeAndMagistratesOutcomeIsEmpty_whenUpdateIsInvoked_thenFailsBadRequest() throws Exception {
        ApiUpdateCrownCourtRequest apiUpdateCrownCourtRequest = TestModelDataBuilder.getApiUpdateCrownCourtRequest(Boolean.TRUE);
        apiUpdateCrownCourtRequest.setCaseType(CaseType.INDICTABLE);
        apiUpdateCrownCourtRequest.setMagCourtOutcome(null);
        stubForOAuth();

        wiremock.stubFor(get(urlMatching("/api/internal/v1/assessment/rep-orders/cc-outcome/.*"))
                .willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(objectMapper.writeValueAsString(List.of(
                                        TestModelDataBuilder.getRepOrderCCOutcomeDTO(1, CrownCourtOutcome.SUCCESSFUL.getCode(),
                                                LocalDateTime.of(2022, 3, 7, 10, 1, 25)
                                        )))
                                ))
        );
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(apiUpdateCrownCourtRequest), ENDPOINT_URL + UPDATE_CC_URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME));
    }

    @Test
    void givenValidContent_whenApiResponseIsError_thenUpdateIsFails() throws Exception {
        var stubPath = "/api/internal/v1/assessment/rep-orders/cc-outcome/reporder/" + TEST_REP_ID;
        ApiUpdateCrownCourtRequest apiUpdateCrownCourtRequest = TestModelDataBuilder.getApiUpdateCrownCourtRequest(Boolean.TRUE);
        stubForOAuth();

        wiremock.stubFor(get(stubPath)
                .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.code())
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                ));
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(apiUpdateCrownCourtRequest), ENDPOINT_URL + UPDATE_CC_URL))
                .andExpect(jsonPath("$.message", containsString("500 Internal Server Error")))
                .andExpect(jsonPath("$.message", containsString(stubPath)));
    }

    @Test
    void givenValidContent_whenUpdateIsInvoked_thenUpdateIsSuccess() throws Exception {

        ApiUpdateCrownCourtRequest apiUpdateCrownCourtRequest = TestModelDataBuilder.getApiUpdateCrownCourtRequest(Boolean.TRUE);
        apiUpdateCrownCourtRequest.setCaseType(CaseType.APPEAL_CC);
        stubForUpdateCC();
        stubForIoJAppeal();
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(apiUpdateCrownCourtRequest), ENDPOINT_URL + UPDATE_CC_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.crownCourtSummary.repOrderDecision").value(Constants.GRANTED_PASSED_MEANS_TEST))
                .andExpect(jsonPath("$.crownCourtSummary.repType").value(Constants.CROWN_COURT_ONLY))
                .andExpect(jsonPath("$.crownCourtSummary.evidenceFeeLevel").value(EvidenceFeeLevel.LEVEL1.getFeeLevel()))
                .andExpect(jsonPath("$.crownCourtSummary.repOrderCrownCourtOutcome[0].outcome").value(CrownCourtOutcome.SUCCESSFUL.getCode()))
                .andExpect(jsonPath("$.modifiedDateTime").isNotEmpty());

    }

    private void stubForUpdateCC() throws Exception {

        stubForOAuth();

        wiremock.stubFor(get(urlMatching("/api/internal/v1/assessment/rep-orders/cc-outcome/.*"))
                .willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(objectMapper.writeValueAsString(List.of(
                                        TestModelDataBuilder.getRepOrderCCOutcomeDTO(1, CrownCourtOutcome.SUCCESSFUL.getCode(),
                                                LocalDateTime.of(2022, 3, 7, 10, 1, 25)
                                        )))
                                ))
        );
        wiremock.stubFor(put("/api/internal/v1/assessment/rep-orders")
                .willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(objectMapper.writeValueAsString(TestModelDataBuilder.getRepOrderDTO()))
                )
        );

        wiremock.stubFor(head(anyUrl())
                .willReturn(
                        WireMock.ok()
                                .withHeader("X-Total-Records", String.valueOf(0))
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                )
        );

        wiremock.stubFor(post("/api/internal/v1/evidence/calculate-evidence-fee")
                .willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(objectMapper.writeValueAsString(TestModelDataBuilder.getApiCalculateEvidenceFeeResponse()))
                )
        );
    }


    private void stubForIoJAppeal() throws JsonProcessingException {
        wiremock.stubFor(get("/api/internal/v1/assessment/ioj-appeal/repId/" + TEST_REP_ID + "/current-passed")
                .willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(objectMapper.writeValueAsString(TestModelDataBuilder.getIOJAppealDTO()))
                )
        );
    }
}