package uk.gov.justice.laa.crime.crowncourt.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.crime.common.model.proceeding.common.ApiIOJSummary;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiDetermineMagsRepDecisionRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiDetermineMagsRepDecisionResponse;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.service.MagsProceedingService;
import uk.gov.justice.laa.crime.crowncourt.tracing.TraceIdHandler;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.util.RequestBuilderUtils;

@DirtiesContext
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(MagsProceedingController.class)
class MagsProceedingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TraceIdHandler traceIdHandler;

    @MockitoBean
    private MagsProceedingService magsProceedingService;

    @MockitoBean
    private DeadLetterMessageService deadLetterMessageService;


    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings/determine-mags-rep-decision";

    @Test
    void determineMagsRepDecision_Success() throws Exception {
        var determineMagsRepDecisionRequestJson = BuildRequestJson();

        var determineMagsRepDecisionResponse = new ApiDetermineMagsRepDecisionResponse()
                .withDecisionResult(TestModelDataBuilder.getMagsDecisionResult());

        when(magsProceedingService.determineMagsRepDecision(any(CrownCourtDTO.class)))
                .thenReturn(TestModelDataBuilder.getMagsDecisionResult());

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, determineMagsRepDecisionRequestJson, ENDPOINT_URL))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(determineMagsRepDecisionResponse)));
    }

    @Test
    void determineMagsRepDecision_Success_NullDecisionReason() throws Exception {
        var determineMagsRepDecisionRequestJson = BuildRequestJson();

        when(magsProceedingService.determineMagsRepDecision(any(CrownCourtDTO.class)))
                .thenReturn(null);

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.POST, determineMagsRepDecisionRequestJson, ENDPOINT_URL))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(new ApiDetermineMagsRepDecisionResponse())));
    }

    private String BuildRequestJson() throws JsonProcessingException {
        var apiDetermineMagsRepDecisionRequest = new ApiDetermineMagsRepDecisionRequest()
                .withCaseType(CaseType.INDICTABLE)
                .withRepId(TestModelDataBuilder.TEST_REP_ID)
                .withIojAppeal(new ApiIOJSummary().withIojResult("PASS"))
                .withPassportAssessment(TestModelDataBuilder.getPassportAssessment())
                .withFinancialAssessment(TestModelDataBuilder.getFinancialAssessment())
                .withUserSession(TestModelDataBuilder.getApiUserSession(true));
        return objectMapper.writeValueAsString(apiDetermineMagsRepDecisionRequest);
    }

    @Test
    void determineMagsRepDecision_ServerError_RequestBodyIsMissing() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "", ENDPOINT_URL))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void determineMagsRepDecision_BadRequest_RequestEmptyBody() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.POST, "{}", ENDPOINT_URL))
                .andExpect(status().isBadRequest());
    }
}
