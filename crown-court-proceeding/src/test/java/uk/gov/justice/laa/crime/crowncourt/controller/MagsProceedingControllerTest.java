package uk.gov.justice.laa.crime.crowncourt.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.crime.commons.tracing.TraceIdHandler;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.model.MagsDecisionResult;
import uk.gov.justice.laa.crime.crowncourt.model.common.ApiIOJSummary;
import uk.gov.justice.laa.crime.crowncourt.model.request.ApiDetermineMagsRepDecisionRequest;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiDetermineMagsRepDecisionResponse;
import uk.gov.justice.laa.crime.crowncourt.service.MagsProceedingService;
import uk.gov.justice.laa.crime.enums.CaseType;
import uk.gov.justice.laa.crime.enums.DecisionReason;
import uk.gov.justice.laa.crime.util.RequestBuilderUtils;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(MagsProceedingController.class)
class MagsProceedingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MagsProceedingService magsProceedingService;

    @MockBean
    private TraceIdHandler traceIdHandler;

    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings/determine-mags-rep-decision";

    @Test
    void determineMagsRepDecision_Success() throws Exception {
        var determineMagsRepDecisionRequestJson = BuildRequestJson();

        var determineMagsRepDecisionResponse = new ApiDetermineMagsRepDecisionResponse()
                .withDecisionResult(
                        MagsDecisionResult.builder()
                                .decisionDate(LocalDate.now())
                                .decisionReason(DecisionReason.GRANTED)
                                .timestamp(TestModelDataBuilder.TEST_DATE_MODIFIED)
                                .build()
                );

        when(magsProceedingService.determineMagsRepDecision(any(CrownCourtDTO.class)))
                .thenReturn(MagsDecisionResult.builder()
                                    .decisionReason(DecisionReason.GRANTED)
                                    .decisionDate(LocalDate.now())
                                    .timestamp(TestModelDataBuilder.TEST_DATE_MODIFIED)
                                    .build()
                );

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