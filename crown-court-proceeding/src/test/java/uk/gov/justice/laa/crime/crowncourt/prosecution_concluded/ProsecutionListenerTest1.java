package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded;


import cloud.localstack.Localstack;
import cloud.localstack.ServiceName;
import cloud.localstack.awssdkv1.TestUtils;
import cloud.localstack.docker.LocalstackDockerExtension;
import cloud.localstack.docker.annotation.LocalstackDockerConfiguration;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;


@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(services = {ServiceName.SQS})
@Testcontainers
@SpringBootTest
public class ProsecutionListenerTest1 {

    private static String QUEUE_NAME = "crime-apps-dev-prosecution-concluded-queue";

    private static final LocalstackDockerConfiguration DOCKER_CONFIG = LocalstackDockerConfiguration.builder()
            .randomizePorts(false)
            .build();


    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("cloud-platform.aws.sqs.queue.prosecutionConcluded", () -> QUEUE_NAME);
        registry.add("spring.cloud.aws.sqs.endpoint", () -> Localstack.INSTANCE.getEndpointSQS());
        registry.add("cloud-platform.aws.sqs.accesskey", () -> "test");
        registry.add("cloud-platform.aws.sqs.secretkey", () -> "test");
        registry.add("cloud-platform.aws.sqs.region", () -> "us-east-1");
        registry.add("feature.prosecution-concluded-listener.enabled", () -> "true");
    }

    @BeforeEach
    void setUp() {
        Localstack.INSTANCE.stop();
        Localstack.INSTANCE.startup(DOCKER_CONFIG);
    }

    @Test
    public void startup() {
        AmazonSQS amazonSQS = TestUtils.getClientSQS();
        String url = amazonSQS.createQueue(QUEUE_NAME).getQueueUrl();
        SendMessageResult sendMessageResult = amazonSQS.sendMessage(url, getSqsMessagePayload());
    }

    @AfterEach
    void stop() {
        Localstack.INSTANCE.stop();
    }


    private String getSqsMessagePayload() {
        return """
                {
                   prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                   defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                   isConcluded: true,
                   hearingIdWhereChangeOccurred : 61600a90-89e2-4717-aa9b-a01fc66130c1,
                   offenceSummary: [
                           {
                               offenceId: ed0e9d59-cc1c-4869-8fcd-464caf770744,
                               offenceCode: PT00011,
                               proceedingsConcluded: true,
                               proceedingsConcludedChangedDate: 2022-02-01,
                               plea: {
                                   originatingHearingId: 61600a90-89e2-4717-aa9b-a01fc66130c1,
                                   value: GUILTY,
                                   pleaDate: 2022-02-01
                               },
                               verdict: {
                                   verdictDate: 2022-02-01,
                                   originatingHearingId: 61600a90-89e2-4717-aa9b-a01fc66130c1,
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
                       maatId: 6039349,
                       metadata: {
                           laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                       }
                   }""";
    }
}

