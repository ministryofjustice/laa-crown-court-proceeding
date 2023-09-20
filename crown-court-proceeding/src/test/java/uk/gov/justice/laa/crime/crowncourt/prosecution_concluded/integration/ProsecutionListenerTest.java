package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.integration;

import cloud.localstack.Localstack;
import cloud.localstack.ServiceName;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testcontainers.shaded.org.awaitility.Awaitility.with;

@ExtendWith({LocalstackDockerExtension.class})
@LocalstackDockerProperties(services = {ServiceName.SQS}, hostNameResolver = HostNameResolverImpl.class)
//@Testcontainers
@SpringBootTest(classes = {CrownCourtProceedingApplication.class})
@AutoConfigureWireMock(port = 9999)
@DirtiesContext
public class ProsecutionListenerTest {

    private static final String QUEUE_NAME = "crime-apps-dev-prosecution-concluded-queue";
    private static AmazonSQS amazonSQS;
    private static String queueUrl;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.sqs.endpoint", Localstack.INSTANCE::getEndpointSQS);
        registry.add("feature.prosecution-concluded-listener.enabled", () -> "true");
        System.out.println("getEndpointSQS : " + Localstack.INSTANCE.getEndpointSQS());
        System.out.println("LocalStack running Localstack.INSTANCE.getLocalStackContainer(): " + Localstack.INSTANCE.getLocalStackContainer().isRunning());
        System.out.println("LocalStack running : " + Localstack.INSTANCE.isRunning());
    }

    @BeforeAll
    static void beforeAll() throws JsonProcessingException {
        System.out.println("LocalStack running Localstack.INSTANCE.getLocalStackContainer(): " + Localstack.INSTANCE.getLocalStackContainer().isRunning());
        amazonSQS = TestUtils.getClientSQS();
        System.out.println("LocalStack running : " + Localstack.INSTANCE.isRunning());
        queueUrl = amazonSQS.createQueue(QUEUE_NAME).getQueueUrl();
        System.out.println("queueUrl : " + queueUrl);
        System.out.println("region : " + Localstack.getDefaultRegion());
        stubForOAuth();
    }

    @Test
    public void givenAValidMessage_whenProsecutionConcludedListenerIsInvoked_thenUpdateCaseConclusion() {
        amazonSQS.sendMessage(queueUrl, getSqsMessagePayload(5635566, true));
        with().pollDelay(5, SECONDS).await().until(() -> true);
        verify(exactly(1), getRequestedFor(urlEqualTo("/api/internal/v1/assessment/wq-link-register/5635566")));
    }

    @Test
    public void givenAValidMessageAndCaseIsNotConcluded_whenProsecutionConcludedListenerIsInvoked_thenShouldNotUpdateConclusion() {
        amazonSQS.sendMessage(queueUrl, getSqsMessagePayload(6766767, false));
        with().pollDelay(5, SECONDS).await().until(() -> true);
        verify(exactly(0), getRequestedFor(urlEqualTo("/api/internal/v1/assessment/wq-link-register/5635566")));
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

