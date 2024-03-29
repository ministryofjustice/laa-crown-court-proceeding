package integration;

import cloud.localstack.awssdkv1.TestUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;
import uk.gov.justice.laa.crime.crowncourt.repository.ProsecutionConcludedRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.with;

@DirtiesContext
@Testcontainers
@SpringBootTest(classes = {CrownCourtProceedingApplication.class})
@AutoConfigureWireMock(port = 9999)
@AutoConfigureObservability
class ProsecutionListenerTest {

    private static final String QUEUE_NAME = "crime-apps-dev-prosecution-concluded-queue";
    @Container
    static LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
            .withServices(LocalStackContainer.Service.SQS);
    private static AmazonSQS amazonSQS;
    private static String queueUrl;

    @Autowired
    private ProsecutionConcludedRepository prosecutionConcludedRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localStack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        registry.add("feature.prosecution-concluded-listener.enabled", () -> "true");
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        amazonSQS = TestUtils.getClientSQS(localStack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        queueUrl = amazonSQS.createQueue(QUEUE_NAME).getQueueUrl();
        stubForOAuth();
    }

    private static void stubForOAuth() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> token = Map.of(
                "expires_in", 3600,
                "token_type", "Bearer",
                "access_token", UUID.randomUUID()
        );

        stubFor(
                post("/oauth2/token").willReturn(
                        WireMock.ok()
                                .withHeader("Content-Type", String.valueOf(MediaType.APPLICATION_JSON))
                                .withBody(mapper.writeValueAsString(token))
                )
        );
    }

    @Test
    @Order(1)
    void givenAValidMessage_whenProsecutionConcludedListenerIsInvoked_thenShouldCreateWithPending() {
        prosecutionConcludedRepository.deleteAll();
        amazonSQS.sendMessage(queueUrl, getSqsMessagePayload(5635566, true));
        with().pollDelay(10, SECONDS).pollInterval(10, SECONDS).await().atMost(60, SECONDS)
                .until(() -> !prosecutionConcludedRepository.getByMaatId(5635566).isEmpty());
        List<ProsecutionConcludedEntity> prosecutionConcludedEntities = prosecutionConcludedRepository.getByMaatId(5635566);

        assertThat(prosecutionConcludedEntities).isNotEmpty();
        assertThat(prosecutionConcludedEntities.get(0).getStatus()).isEqualTo(CaseConclusionStatus.PENDING.name());
        verify(exactly(1), getRequestedFor(urlEqualTo("/api/internal/v1/assessment/wq-hearing/908ad01e-5a38-4158-957a-0c1d1a783862/maatId/5635566")));
        verify(exactly(1), getRequestedFor(urlEqualTo("/api/internal/v1/assessment/reservations/5635566")));

    }

    @Test
    @Order(2)
    void givenAValidMessage_whenProsecutionConcludedListenerIsInvoked_thenShouldUpdateCaseConclusion() {
        amazonSQS.sendMessage(queueUrl, getSqsMessagePayload(5635566, true));
        with().pollDelay(10, SECONDS).pollInterval(10, SECONDS).await().atMost(60, SECONDS)
                .until(() -> prosecutionConcludedRepository.getByMaatId(5635566).get(0).getStatus(),
                        Predicate.isEqual(CaseConclusionStatus.PROCESSED.name()));
        List<ProsecutionConcludedEntity> prosecutionConcludedEntities = prosecutionConcludedRepository.getByMaatId(5635566);

        assertThat(prosecutionConcludedEntities).isNotEmpty();
        assertThat(prosecutionConcludedEntities.get(0).getStatus()).isEqualTo(CaseConclusionStatus.PROCESSED.name());
        prosecutionConcludedRepository.deleteAll();
        verify(exactly(1), getRequestedFor(urlEqualTo("/api/internal/v1/assessment/wq-link-register/5635566")));
        verify(exactly(1), getRequestedFor(urlEqualTo("/api/internal/v1/assessment/offence/case/665421")));
        verify(exactly(1), putRequestedFor(urlEqualTo("/api/internal/v1/assessment/crown-court/update-cc-sentence")));
    }

    private String getSqsMessagePayload(Integer maatId, boolean isCaseConcluded) {
        return """
                {
                   prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                   defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                   isConcluded: """ + isCaseConcluded + """
                ,hearingIdWhereChangeOccurred : 908ad01e-5a38-4158-957a-0c1d1a783862,
                offenceSummary: [
                        {
                            offenceId: ed0e9d59-cc1c-4869-8fcd-464caf770744,
                            offenceCode: PT00011,
                            proceedingsConcluded: true,
                            proceedingsConcludedChangedDate: 2022-02-01,
                            plea: {
                                originatingHearingId: 908ad01e-5a38-4158-957a-0c1d1a783862,
                                value: GUILTY,
                                pleaDate: 2022-02-01
                            },
                            verdict: {
                                verdictDate: 2022-02-01,
                                originatingHearingId: 908ad01e-5a38-4158-957a-0c1d1a783862,
                                verdictType: {
                                    description: GUILTY,
                                    category: GUILTY,
                                    categoryType: GUILTY,
                                    sequence: 4126,
                                    verdictTypeId: null
                                }
                            }
                        }
                    ],
                    maatId: """ + maatId + """
                    ,metadata: {
                        laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                    }
                }""";
    }
}

