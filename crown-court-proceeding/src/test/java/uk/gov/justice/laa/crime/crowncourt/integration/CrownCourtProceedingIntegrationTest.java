package uk.gov.justice.laa.crime.crowncourt.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.justice.laa.crime.crowncourt.common.Constants;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.model.request.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.CrownCourtOutcome;
import uk.gov.justice.laa.crime.enums.EvidenceFeeLevel;
import uk.gov.justice.laa.crime.util.RequestBuilderUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CrownCourtProceedingIntegrationTest extends WiremockIntegrationTest {

    private static final boolean IS_VALID = true;
    private static final String ERROR_MSG = "Call to service failed. Retries exhausted: 2/2.";
    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings";

    private static final String UPDATE_CC_URL = "/update-crown-court";

    private static final String CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME =
            "Cannot have Crown Court outcome without Mags Court outcome";

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

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
    void givenAInvalidContent_whenProcessRepOrderIsInvoked_thenFailsBadRequest() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiProcessRepOrderRequest(!IS_VALID);
        var applicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, applicationRequestJson, ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenAValidContent_whenApiResponseIsError_thenProcessRepOrderIsFails() throws Exception {
        var apiProcessRepOrderRequest = TestModelDataBuilder.getApiProcessRepOrderRequest(Boolean.TRUE);
        apiProcessRepOrderRequest.setCaseType(CaseType.APPEAL_CC);

        stubForOAuth();
        wiremock.stubFor(get("/api/internal/v1/assessment/ioj-appeal/repId/91919/current-passed")
                .willReturn(
                        WireMock.serverError()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                )
        );

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
        stubForOAuth();
        stubForIoJAppeal();
        MvcResult result = mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, processRepOrderRequestJson, ENDPOINT_URL))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo(objectMapper.writeValueAsString(processRepOrderResponse));
    }


    @Test
    void givenAEmptyContent_whenUpdateApplicationIsInvoked_thenFailsBadRequest() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void givenAEmptyOAuthToken_whenUpdateApplicationIsInvoked_thenFailsUnauthorizedAccess() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, "{}", ENDPOINT_URL, Boolean.FALSE))
                .andExpect(status().isUnauthorized());
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
        wiremock.stubFor(put("/api/internal/v1/assessment/rep-orders")
                .willReturn(
                        WireMock.serverError()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                )
        );

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(
                                TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE)), ENDPOINT_URL))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(ERROR_MSG));
    }

    @Test
    void givenAValidContent_whenUpdateApplicationIsInvoked_thenUpdateApplicationIsSuccess() throws Exception {
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
    void givenAIndictableCaseTypeAndMagistratesOutcomeIsEmpty_whenUpdateIsInvoked_thenFailsBadRequest() throws Exception {
        ApiUpdateApplicationRequest apiUpdateApplicationRequest = TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE);
        apiUpdateApplicationRequest.setCaseType(CaseType.INDICTABLE);
        apiUpdateApplicationRequest.setMagCourtOutcome(null);
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
                        HttpMethod.PUT, objectMapper.writeValueAsString(apiUpdateApplicationRequest), ENDPOINT_URL + UPDATE_CC_URL))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME));
    }

    @Test
    void givenAValidContent_whenApiResponseIsError_thenUpdateIsFails() throws Exception {
        ApiUpdateApplicationRequest apiUpdateApplicationRequest = TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE);
        stubForOAuth();

        wiremock.stubFor(get(urlMatching("/api/internal/v1/assessment/rep-orders/cc-outcome/.*"))
                .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.code())
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                ));
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(apiUpdateApplicationRequest), ENDPOINT_URL + UPDATE_CC_URL))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value(ERROR_MSG));
    }

    @Test
    void givenAValidContent_whenUpdateIsInvoked_thenUpdateIsSuccess() throws Exception {

        ApiUpdateApplicationRequest apiUpdateApplicationRequest = TestModelDataBuilder.getApiUpdateApplicationRequest(Boolean.TRUE);
        apiUpdateApplicationRequest.setCaseType(CaseType.APPEAL_CC);
        stubForUpdateCC();
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, objectMapper.writeValueAsString(apiUpdateApplicationRequest), ENDPOINT_URL + UPDATE_CC_URL))
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
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withHeader("Content-Length", "0")
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
        wiremock.stubFor(get("/api/internal/v1/assessment/ioj-appeal/repId/91919/current-passed")
                .willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(objectMapper.writeValueAsString(TestModelDataBuilder.getIOJAppealDTO()))
                )
        );
    }
}