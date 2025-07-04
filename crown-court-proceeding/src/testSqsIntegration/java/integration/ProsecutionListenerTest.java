package integration;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.setScenarioState;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.with;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
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
import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.repository.DeadLetterMessageRepository;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus;
import uk.gov.justice.laa.crime.util.FileUtils;

@EnableWireMock
@DirtiesContext
@Testcontainers
@AutoConfigureObservability
@SpringBootTest(classes = {CrownCourtProceedingApplication.class})
class ProsecutionListenerTest {

    private static String queueUrl;
    private static AmazonSQS amazonSQS;

    private static final String AQUITTED = "AQUITTED";
    private static final String CONVICTED = "CONVICTED";
    private static final String PART_CONVICTED = "PART CONVICTED";
    private static final String QUEUE_NAME = "crime-apps-dev-prosecution-concluded-queue";
    private static final String CC_OUTCOME_URL = "/api/internal/v1/assessment/crown-court/updateCCOutcome";

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
            .withServices(LocalStackContainer.Service.SQS).withEnv("LOCALSTACK_HOST", "127.0.0.1");

    @Autowired
    private Gson gson;

    @InjectWireMock
    private static WireMockServer wiremock;

    @Autowired
    private DeadLetterMessageRepository deadLetterMessageRepository;

    @Autowired
    private ProsecutionConcludedRepository prosecutionConcludedRepository;


    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localStack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        registry.add("feature.prosecution-concluded-listener.enabled", () -> "true");
    }

    @BeforeEach
    void setup() throws JsonProcessingException {
        stubForOAuth();
    }

    @BeforeAll
    static void setupSqs() {
        amazonSQS = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        localStack.getEndpointOverride(Service.SQS).toString(),
                        localStack.getRegion()
                ))
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials("test", "test")
                ))
                .build();
        queueUrl = amazonSQS.createQueue(QUEUE_NAME).getQueueUrl();
    }

    private static void stubForOAuth() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> token = Map.of(
                "expires_in", 3600,
                "token_type", "Bearer",
                "access_token", UUID.randomUUID()
        );

        wiremock.stubFor(
                post("/oauth2/token").willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(mapper.writeValueAsString(token))
                )
        );
    }

    @Test
    void givenAValidMessage_whenListenerIsInvoked_thenShouldCreateWithPending() {
        prosecutionConcludedRepository.deleteAll();
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadPleaAndVerdict.json"));
        setScenarioState("reservations", "Started");
        with().pollDelay(10, SECONDS).pollInterval(10, SECONDS).await().atMost(30, SECONDS)
                .until(() -> !prosecutionConcludedRepository.getByMaatId(5635566).isEmpty());
        List<ProsecutionConcludedEntity> prosecutionConcludedEntities = prosecutionConcludedRepository.getByMaatId(5635566);

        assertThat(prosecutionConcludedEntities).isNotEmpty();
        assertThat(prosecutionConcludedEntities.get(0).getStatus()).isEqualTo(CaseConclusionStatus.PENDING.name());
        verify(exactly(1), getRequestedFor(urlEqualTo("/api/internal/v1/assessment/wq-hearing/908ad01e-5a38-4158-957a-0c1d1a783862/maatId/5635566")));
        verify(exactly(1), getRequestedFor(urlEqualTo("/api/internal/v1/assessment/reservations/5635566")));
    }

    @Test
    void givenAnInvalidMessageWithMissingMaatId_whenListenerIsInvoked_thenMessageIsSavedAsADeadLetterMessage() {
        deadLetterMessageRepository.deleteAll();
        String queueMessage = FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadPleaWithMissingMaatId.json");
        ProsecutionConcluded expectedMessage = gson.fromJson(queueMessage, ProsecutionConcluded.class);

        amazonSQS.sendMessage(queueUrl, queueMessage);
        setScenarioState("reservations", "Started");
        with().pollDelay(10, SECONDS).pollInterval(10, SECONDS).await().atMost(30, SECONDS)
            .until(() -> !deadLetterMessageRepository.findAll().isEmpty());

        List<DeadLetterMessageEntity> deadLetterMessages = deadLetterMessageRepository.findAll();
        assertThat(deadLetterMessages).hasSize(1);

        DeadLetterMessageEntity deadLetterMessageEntity = deadLetterMessages.get(0);
        ProsecutionConcluded actualMessage = deadLetterMessageEntity.getMessage();
        assertThat(actualMessage).isEqualTo(expectedMessage);
        assertThat(deadLetterMessageEntity.getDeadLetterReason()).isEqualTo(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL);
    }

    @Test
    void givenAnInvalidMessageWithInvalidMaatId_whenListenerIsInvoked_thenMessageIsNotSaved() {
        deadLetterMessageRepository.deleteAll();
        String queueMessage = FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadPleaWithInvalidMaatId.json");
        amazonSQS.sendMessage(queueUrl, queueMessage);
        setScenarioState("reservations", "Started");
        with().pollDelay(10, SECONDS).pollInterval(10, SECONDS).await().atMost(30, SECONDS)
            .until(() -> deadLetterMessageRepository.findAll().isEmpty());

        List<DeadLetterMessageEntity> deadLetterMessages = deadLetterMessageRepository.findAll();
        assertThat(deadLetterMessages).isEmpty();
    }

    @Test
    void givenPleaIsGuiltyAndNoVerdict_whenListenerIsInvoked_thenOutcomeIsConvicted() {
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadPleaWithGuilty.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests.get(requests.size() - 1).getBodyAsString()).isEqualTo(getExpectedRequest(CONVICTED));
    }

    @Test
    void givenAPleaNotGuiltyAndNoVerdict_whenListenerIsInvoked_thenOutcomeIsAquitted() {
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadPleaWithNotGuilty.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests.get(requests.size() - 1).getBodyAsString()).isEqualTo(getExpectedRequest(AQUITTED, null));
    }

    @Test
    void givenAVerdictIsGuiltyAndNoPlea_whenListenerIsInvoked_thenOutcomeIsAConvicted() {
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadVerdictWithGuilty.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests.get(requests.size() - 1).getBodyAsString()).isEqualTo(getExpectedRequest(CONVICTED));
    }


    @Test
    void givenAVerdictIsNotGuiltyAndNoPlea_whenListenerIsInvoked_thenOutcomeIsAAquitted() {
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadVerdictWithNotGuilty.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests.get(requests.size() - 1).getBodyAsString()).isEqualTo(getExpectedRequest(AQUITTED, null));
    }

    @Test
    void givenAPleaIsGuiltyAndVerdictIsGuilty_whenListenerIsInvoked_thenOutcomeIsConvicted() {
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadPleaAndVerdictWithGuilty.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests.get(requests.size() - 1).getBodyAsString()).isEqualTo(getExpectedRequest(CONVICTED));
    }

    @Test
    void givenAPleaIsNotGuiltyAndVerdictIsNotGuilty_whenListenerIsInvoked_thenOutcomeIsAquitted() {
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadPleaAndVerdictWithNotGuilty.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests.get(requests.size() - 1).getBodyAsString()).isEqualTo(getExpectedRequest(AQUITTED, null));
    }

    @Test
    void givenAPleaIsNotGuiltyAndVerdictIsGuilty_whenListenerIsInvoked_thenOutcomeIsConvicted() {
       /* amazonSQS.sendMessage(queueUrl, getSqsMessagePayloadWithPleaAndVerdict(5635567, "" +
                NOT_GUILTY, GUILTY));*/
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsPayloadPleaIsNotGuiltyAndVerdictIsGuilty.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests.get(requests.size() - 1).getBodyAsString()).isEqualTo(getExpectedRequest(CONVICTED));
    }

    @Test
    void givenAMultipleOffenceWithPleaAndNoVerdict_whenListenerIsInvoked_thenOutcomeIsAConvicted() {
        /*amazonSQS.sendMessage(queueUrl, getSqsMessagePayloadWithMultiplePlea(5635567, GUILTY));*/

        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsMultipleOffenceWithPleaIsGuilty.json"));

        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests.get(requests.size() - 1).getBodyAsString()).isEqualTo(getExpectedRequest(CONVICTED));
    }

    @Test
    void givenAMultipleOffence_whenPleaIsGuiltyAndVerdictIsNotGuilty_thenOutcomeIsAPartConvicted() {
        /*amazonSQS.sendMessage(queueUrl, getPayloadWithMultiplePleaAndVerdict(5635567, GUILTY));*/

        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsMultipleOffenceWithPleaAndVerdictIsNotGuilty.json"));

        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        LoggedRequest request = requests.get(requests.size() - 1);
        assertThat(request.getBodyAsString()).isEqualTo(getExpectedRequest(PART_CONVICTED));
    }

    @Test
    void givenAMultipleOffence_withPleaIsNotGuiltyAndVerdictIsNotGuilty_thenOutcomeIsAquitted() {
        /*amazonSQS.sendMessage(queueUrl, getPayloadWithMultiplePleaAndVerdict(5635567, NOT_GUILTY));*/
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsMultipleOffenceWithNotGuilty.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        LoggedRequest request = requests.get(requests.size() - 1);
        assertThat(request.getBodyAsString()).isEqualTo(getExpectedRequest(AQUITTED, null));
    }

    @Test
    void givenAMultipleOffence_withNoPleaAndNoVerdictInformation_thenOutcomeIsAquitted() {
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsMultipleOffenceWithNoPleaAndNoVerdict.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        LoggedRequest request = requests.get(requests.size() - 1);
        assertThat(request.getBodyAsString()).isEqualTo(getExpectedRequest(AQUITTED, null));
    }

    @Test
    void givenAMultipleOffence_withNoPleaNoVerdictInformationAndIsConvictedResult_thenOutcomeIsConvicted() {
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsMultipleOffenceWithNoPleaNoVerdictAndIsConvictedResult.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        LoggedRequest request = requests.get(requests.size() - 1);
        assertThat(request.getBodyAsString()).isEqualTo(getExpectedRequest(AQUITTED, null));
    }

    @Test
    void givenAMultipleOffence_withNoPleaNoVerdictInformationAndNotIsConvictedResult_thenOutcomeIsAquitted() {
        amazonSQS.sendMessage(queueUrl, FileUtils.readFileToString("data/prosecution_concluded/SqsMultipleOffenceWithNoPleaNoVerdictAndNotIsConvictedResult.json"));
        setScenarioState("reservations", "State 2");

        with().pollDelay(10, SECONDS).pollInterval(5, SECONDS).await().atMost(60, SECONDS)
                .untilAsserted(() -> verify(putRequestedFor(urlEqualTo(CC_OUTCOME_URL))));

        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        LoggedRequest request = requests.get(requests.size() - 1);
        assertThat(request.getBodyAsString()).isEqualTo(getExpectedRequest(AQUITTED, null));
    }

    private String getExpectedRequest(String outcome, String imprisoned) {
        return "{\"repId\":5635567,\"ccOutcome\":\"" + outcome + "\",\"benchWarrantIssued\":null,\"appealType\":\"ACN\",\"imprisoned\":" + imprisoned + ",\"caseNumber\":\"21GN1208521\",\"crownCourtCode\":\"433\"}";
    }

    private String getExpectedRequest(String outcome) {
        return "{\"repId\":5635567,\"ccOutcome\":\"" + outcome + "\",\"benchWarrantIssued\":null,\"appealType\":\"ACN\",\"imprisoned\":\"N\",\"caseNumber\":\"21GN1208521\",\"crownCourtCode\":\"433\"}";
    }
}


