package integration;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.setScenarioState;
import static uk.gov.justice.laa.crime.util.FileUtils.readFileToString;

import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener.ProsecutionConcludedListenerHelper;
import uk.gov.justice.laa.crime.crowncourt.repository.DeadLetterMessageRepository;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;

import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.wiremock.spring.InjectWireMock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

abstract class AbstractProsecutionConcludedTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @InjectWireMock
    protected static WireMockServer wiremock;

    @Autowired
    protected DeadLetterMessageRepository deadLetterMessageRepository;

    @Autowired
    protected ProsecutionConcludedRepository prosecutionConcludedRepository;

    @Autowired
    protected ProsecutionConcludedListenerHelper prosecutionConcludedListenerHelper;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Don't enable the listener, it will try to bind to SQS which will fail
        registry.add("feature.prosecution-concluded-listener.enabled", () -> "false");
        // Use the Postgres Test Container for the DB
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @BeforeEach
    void setup() throws JsonProcessingException {
        stubForOAuth();

        // Setup some common pre-conditions
        givenTheDBIsEmpty();
        givenTheMaatRecordIsNotLocked();
    }

    private static void stubForOAuth() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> token =
                Map.of("expires_in", 3600, "token_type", "Bearer", "access_token", UUID.randomUUID());

        wiremock.stubFor(post("/oauth2/token")
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(mapper.writeValueAsString(token))));
    }

    public enum ReservationScenarioState {
        STARTED("Started"),
        STATE_2("State 2");

        private final String value;

        ReservationScenarioState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    protected static @NonNull MessageHeaders getDefaultMessageHeaders() {
        Map<String, Object> headerMap = Map.of("MessageId", "1234567890");
        return new MessageHeaders(headerMap);
    }

    protected static void givenTheMaatRecordIsLocked() {
        // See api_reservations.json
        setScenarioState("reservations", ReservationScenarioState.STARTED.getValue());
    }

    protected static void givenTheMaatRecordIsNotLocked() {
        // See api_reservations.json
        setScenarioState("reservations", ReservationScenarioState.STATE_2.getValue());
    }

    protected void givenTheDBIsEmpty() {
        prosecutionConcludedRepository.deleteAll();
        deadLetterMessageRepository.deleteAll();
    }

    protected static String getMessageFromFile(String messageFile) {
        return readFileToString("data/prosecution_concluded/" + messageFile);
    }
}
