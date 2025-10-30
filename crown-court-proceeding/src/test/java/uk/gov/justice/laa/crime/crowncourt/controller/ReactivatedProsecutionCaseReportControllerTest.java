package uk.gov.justice.laa.crime.crowncourt.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.justice.laa.crime.crowncourt.reports.service.ReactivatedProsecutionCaseReportService;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.tracing.TraceIdHandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ReactivatedProsecutionCaseReportController.class)
class ReactivatedProsecutionCaseReportControllerTest {

    private static final String ENDPOINT_URL = "/api/internal/v1/send-report";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TraceIdHandler traceIdHandler;

    @MockitoBean
    private DeadLetterMessageService deadLetterMessageService;

    @MockitoBean
    private ReactivatedProsecutionCaseReportService reactivatedProsecutionCaseReportService;

    @Test
    void shouldInvokeReactivatedProsecutionCaseReportService() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(ENDPOINT_URL)).andExpect(status().isOk());
        verify(reactivatedProsecutionCaseReportService, times(1)).generateReport();
    }
}
