package integration;

import static com.github.tomakehurst.wiremock.client.WireMock.setScenarioState;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.testcontainers.shaded.org.awaitility.Awaitility.with;

import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.repository.DeadLetterMessageRepository;
import uk.gov.justice.laa.crime.util.FileUtils;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;

@EnableWireMock
@Testcontainers
@AutoConfigureObservability
@SpringBootTest(classes = {CrownCourtProceedingApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProsecutionListenerDeadLetterMockTest {

    private static String queueUrl;
    private static AmazonSQS amazonSQS;

    private static final String QUEUE_NAME = "crime-apps-dev-prosecution-concluded-queue";

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
            .withServices(LocalStackContainer.Service.SQS)
            .withEnv("LOCALSTACK_HOST", "127.0.0.1");

    @Autowired
    private Gson gson;

    @InjectWireMock
    private static WireMockServer wiremock;

    @Autowired
    private DeadLetterMessageRepository deadLetterMessageRepository;

    @MockBean
    private DeadLetterMessageRepository mockedDeadLetterMessageRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localStack
                .getEndpointOverride(LocalStackContainer.Service.SQS)
                .toString());
        registry.add("feature.prosecution-concluded-listener.enabled", () -> "true");
    }

    @BeforeEach
    void setup() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> token =
                Map.of("expires_in", 3600, "token_type", "Bearer", "access_token", UUID.randomUUID());
        wiremock.stubFor(WireMock.post("/oauth2/token")
                .willReturn(WireMock.ok()
                        .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                        .withBody(mapper.writeValueAsString(token))));
    }

    @BeforeAll
    static void setupSqs() {
        amazonSQS = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        localStack.getEndpointOverride(Service.SQS).toString(), localStack.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test")))
                .build();
        queueUrl = amazonSQS.createQueue(QUEUE_NAME).getQueueUrl();
    }

    @Test
    void givenValidMessageWithMissingOutcome_whenListenerIsInvoked_thenMessageIsSavedAsADeadLetterMessage() {
        deadLetterMessageRepository.deleteAll();
        String queueMessage =
                FileUtils.readFileToString("data/prosecution_concluded/SqsAppealsPayloadMissingOutcome.json");
        ProsecutionConcluded expectedMessage = gson.fromJson(queueMessage, ProsecutionConcluded.class);
        setReservationState(ProsecutionListenerTest.ReservationScenarioState.STATE_2.getValue());

        amazonSQS.sendMessage(queueUrl, queueMessage);

        with().pollDelay(5, SECONDS)
                .pollInterval(2, SECONDS)
                .await()
                .atMost(30, SECONDS)
                .untilAsserted(() -> {
                    ArgumentCaptor<DeadLetterMessageEntity> captor =
                            ArgumentCaptor.forClass(DeadLetterMessageEntity.class);
                    org.mockito.Mockito.verify(deadLetterMessageRepository, atLeastOnce())
                            .save(captor.capture());
                    DeadLetterMessageEntity saved = captor.getValue();
                    assertThat(saved.getMessage()).isEqualTo(expectedMessage);
                    assertThat(saved.getDeadLetterReason())
                            .isEqualTo(
                                    ProsecutionConcludedValidator
                                            .CANNOT_HAVE_CROWN_COURT_OUTCOME_WITHOUT_MAGS_COURT_OUTCOME);
                });
    }

    private void setReservationState(String state) {
        setScenarioState("reservations", state);
    }
}
