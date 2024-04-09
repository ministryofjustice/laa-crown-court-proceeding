package uk.gov.justice.laa.crime.crowncourt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.crime.commons.tracing.TraceIdHandler;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.service.CrownProceedingService;
import uk.gov.justice.laa.crime.crowncourt.validation.CrownCourtDetailsValidator;
import uk.gov.justice.laa.crime.exception.ValidationException;
import uk.gov.justice.laa.crime.util.RequestBuilderUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ProceedingsController.class)
class ProceedingsControllerTest {

    private static final boolean IS_VALID = true;
    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CrownProceedingService crownProceedingService;

    @MockBean
    private CrownCourtDetailsValidator crownCourtDetailsValidator;

    @MockBean
    private TraceIdHandler traceIdHandler;

    @Test
    void processRepOrder_Success() throws Exception {
        var apiProcessRepOrderRequest =
                TestModelDataBuilder.getApiProcessRepOrderRequest(IS_VALID);
        var processRepOrderRequestJson = objectMapper.writeValueAsString(apiProcessRepOrderRequest);
        var processRepOrderResponse =
                TestModelDataBuilder.getApiProcessRepOrderResponse();

        when(crownProceedingService.processRepOrder(any(CrownCourtDTO.class)))
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
    void updateApplication_Success() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiUpdateApplicationRequest(IS_VALID);
        var updateApplicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);
        var updateApplicationResponse = TestModelDataBuilder.getApiUpdateApplicationResponse();
        when(crownProceedingService.updateApplication(any(CrownCourtDTO.class)))
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
    void givenAValidInput_whenUpdateIsInvoked_thenSuccess() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiUpdateApplicationRequest(IS_VALID);
        var updateRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);
        var updateResponse = TestModelDataBuilder.getApiUpdateCrownCourtOutcomeResponse();
        when(crownProceedingService.update(any(CrownCourtDTO.class)))
                .thenReturn(updateResponse);

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, updateRequestJson, ENDPOINT_URL + "/update-crown-court"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void givenAInValidInput_whenUpdateIsInvoked_RequestObjectFailsValidation() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiUpdateApplicationRequest(!IS_VALID);
        var updateApplicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);

        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, updateApplicationRequestJson, ENDPOINT_URL + "/update-crown-court"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void givenAnInput_whenUpdateIsInvokedAndValidationFails_thenBadRequestResponseIsReturned() throws Exception {
        var apiUpdateApplicationRequest =
                TestModelDataBuilder.getApiUpdateApplicationRequest(IS_VALID);
        var updateApplicationRequestJson = objectMapper.writeValueAsString(apiUpdateApplicationRequest);
        when(crownCourtDetailsValidator.checkCCDetails(any())).thenThrow(new ValidationException("Cannot have Crown Court outcome without Mags Court outcome"));
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(
                        HttpMethod.PUT, updateApplicationRequestJson, ENDPOINT_URL + "/update-crown-court"))
                .andExpect(status().isBadRequest());
    }
}