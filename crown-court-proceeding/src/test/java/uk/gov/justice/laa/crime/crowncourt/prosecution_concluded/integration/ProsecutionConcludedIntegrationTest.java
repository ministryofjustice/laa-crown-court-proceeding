package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.integration.WiremockIntegrationTest;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus;
import uk.gov.justice.laa.crime.util.RequestBuilderUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProsecutionConcludedIntegrationTest extends WiremockIntegrationTest {

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProsecutionConcludedRepository repository;

    private static final String ENDPOINT_URL = "/api/internal/v1/proceedings/prosecution/scheduler";
    private static final Integer INVAID_MAAT_ID = 1111;

    @BeforeEach
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .addFilter(springSecurityFilterChain).build();
        repository.save(TestModelDataBuilder.getProsecutionConcludedEntity());
    }

    @Test
    void givenAInvalidParameter_whenGetCountByMaatIdAndStatusIsInvoked_thenErrorIsThrown() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.HEAD, "{}", ENDPOINT_URL, true))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void givenAInvalidStatusParameter_whenGetNewOffenceCountIsInvoked_thenZeroIsReturned() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.HEAD, "{}", ENDPOINT_URL
                        + "/" + TestModelDataBuilder.TEST_REP_ID + "?status=" + CaseConclusionStatus.PROCESSED.name()))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "0"));
    }

    @Test
    void givenAInvalidMaatIdParameter_whenGetNewOffenceCountIsInvoked_thenZeroIsReturned1() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.HEAD, "{}", ENDPOINT_URL
                        + "/" + INVAID_MAAT_ID))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "0"));
    }

    @Test
    void givenAValidParameter_whenGetNewOffenceCountIsInvoked_thenCountIsReturned() throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.HEAD, "{}", ENDPOINT_URL
                        + "/" + TestModelDataBuilder.TEST_REP_ID, true))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "1"));
    }

    @AfterEach
    public void clearUp() {
        repository.deleteAll();
    }
}