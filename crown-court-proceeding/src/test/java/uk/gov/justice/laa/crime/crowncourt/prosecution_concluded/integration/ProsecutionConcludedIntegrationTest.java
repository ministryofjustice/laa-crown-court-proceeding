package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.integration.WiremockIntegrationTest;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus;
import uk.gov.justice.laa.crime.util.RequestBuilderUtils;

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

    private static final String COUNT_ENDPOINT_URL =
            "/api/internal/v1/proceedings/prosecution-concluded/%s/messages/count";
    private static final Integer NON_EXISTENT_REP_ID = 1111;

    @BeforeEach
    void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .addFilter(springSecurityFilterChain).build();
        ProsecutionConcludedEntity prosecutionConcludedEntity =
                TestModelDataBuilder.getProsecutionConcludedEntity();
        repository.save(prosecutionConcludedEntity);
    }

    @Test
    void givenInvalidParameters_whenGetCountByMaatIdAndStatusIsInvoked_thenErrorIsThrown()
            throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.GET, "{}",
                        String.format(COUNT_ENDPOINT_URL, "INVALID_REP_ID")))
                .andExpect(status().is4xxClientError());
    }

    @ParameterizedTest
    @MethodSource("countUrlProvider")
    void givenNonExistingMaatId_whenGetNewOffenceCountIsInvoked_thenZeroIsReturned(String url,
            int expectedCount) throws Exception {
        mvc.perform(RequestBuilderUtils.buildRequestGivenContent(HttpMethod.GET, "{}", url))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedCount)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private static Stream<Arguments> countUrlProvider() {
        return Stream.of(
                // Scenario 1: Valid repId but with a status that doesn't match any records returns 0.
                Arguments.of(String.format(COUNT_ENDPOINT_URL, TestModelDataBuilder.TEST_REP_ID)
                        + "?status=" + CaseConclusionStatus.PROCESSED.name(), 0),
                // Scenario 2: A non-existent repId returns 0.
                Arguments.of(String.format(COUNT_ENDPOINT_URL, NON_EXISTENT_REP_ID), 0),
                // Scenario 3: Valid repId but with default status returns 1.
                Arguments.of(String.format(COUNT_ENDPOINT_URL, TestModelDataBuilder.TEST_REP_ID),
                        1)
        );
    }

    @AfterEach
    void clearUp() {
        repository.deleteAll();
    }
}