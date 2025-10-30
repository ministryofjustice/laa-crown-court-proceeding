package uk.gov.justice.laa.crime.crowncourt.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import uk.gov.justice.laa.crime.crowncourt.config.IntegrationTestConfiguration;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

@EnableWireMock
@DirtiesContext
@AutoConfigureObservability
@Import(IntegrationTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = IntegrationTestConfiguration.class, webEnvironment = DEFINED_PORT)
public abstract class WiremockIntegrationTest {

    @InjectWireMock
    protected static WireMockServer wiremock;

    protected void stubForOAuth() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> token =
                Map.of("expires_in", 3600, "token_type", "Bearer", "access_token", UUID.randomUUID());

        wiremock.stubFor(post("/oauth2/token")
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(mapper.writeValueAsString(token))));
    }
}
