package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus;
import uk.gov.justice.laa.crime.crowncourt.tracing.TraceIdHandler;

@DirtiesContext
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ProsecutionConcludedController.class)
class ProsecutionConcludedControllerTest {

    private static final String COUNT_ENDPOINT_URL =
            "/api/internal/v1/proceedings/prosecution-concluded/%s/messages/count";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TraceIdHandler traceIdHandler;

    @MockitoBean
    private ProsecutionConcludedDataService service;


    @Test
    void givenIncorrectParameters_whenGetCountByMaatIdAndStatusIsInvoked_thenErrorIsThrown()
            throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(String.format(COUNT_ENDPOINT_URL, "invalid"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidParameters_whenGetCountByMaatIdAndStatusIsInvoked_thenReturnCount()
            throws Exception {
        when(service.getCountByMaatIdAndStatus(TestModelDataBuilder.TEST_REP_ID,
                CaseConclusionStatus.PENDING.name())).thenReturn(1L);
        mvc.perform(MockMvcRequestBuilders.get(
                        String.format(COUNT_ENDPOINT_URL, TestModelDataBuilder.TEST_REP_ID)
                                + "?status=" + CaseConclusionStatus.PENDING.name()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(1)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}