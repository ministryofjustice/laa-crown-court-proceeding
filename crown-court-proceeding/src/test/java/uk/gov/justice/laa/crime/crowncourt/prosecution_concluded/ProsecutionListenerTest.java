package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

@Testcontainers
@SpringBootTest(classes = {CrownCourtProceedingApplication.class})
@ExtendWith(SpringExtension.class)
class ProsecutionListenerTest {

  /*  static StaticCredentialsProvider credentialsProvider;
    private static final DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse("localstack/localstack");

    @Container
    public static LocalStackContainer LOCALSTACK_CONTAINER = new LocalStackContainer(LOCALSTACK_IMAGE)
            .withServices(SQS);

    private static final String QUEUE_NAME = "prosecutionConcluded";

    @Autowired
    SqsTemplate sqsTemplate;

    @Autowired
    ObjectMapper objectMapper;


    @Autowired
    private QueueMessageLogService queueMessageLogService;
    @Autowired
    private Gson gson;
    @Autowired
    private ProsecutionConcludedService prosecutionConcludedService;


   @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.queue", () -> QUEUE_NAME);
        registry.add("spring.cloud.aws.region.static", () -> LOCALSTACK_CONTAINER.getRegion());
        registry.add("spring.cloud.aws.credentials.access-key", () -> LOCALSTACK_CONTAINER.getAccessKey());
        registry.add("spring.cloud.aws.credentials.secret-key", () -> LOCALSTACK_CONTAINER.getSecretKey());
        registry.add(
                "spring.cloud.aws.sqs.endpoint",
                () -> LOCALSTACK_CONTAINER.getEndpointOverride(SQS).toString());
    }

    private static SqsAsyncClient createLocalStackClient() {
        credentialsProvider = StaticCredentialsProvider
                .create(AwsBasicCredentials.create(LOCALSTACK_CONTAINER.getAccessKey(), LOCALSTACK_CONTAINER.getSecretKey()));
        return SqsAsyncClient.builder().credentialsProvider(credentialsProvider)
                .endpointOverride(LOCALSTACK_CONTAINER.getEndpointOverride(SQS)).region(Region.of(LOCALSTACK_CONTAINER.getRegion()))
                .build();
    }

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        LOCALSTACK_CONTAINER.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
    }

    @Test
    void shouldStoreIncomingPurchaseOrderInDatabase() throws Exception {

        Map<String, Object> messageHeaders = Map.of("contentType", "application/json");
        sqsTemplate.send(QUEUE_NAME, new GenericMessage<>(getSqsMessagePayload(), messageHeaders));

    }

   @Bean
    SqsTemplate sqsTemplate() {
        return SqsTemplate.builder().sqsAsyncClient(createLocalStackClient()).build();
    }

    private String getSqsMessagePayload() throws Exception {
        return objectMapper.writeValueAsString(TestModelDataBuilder.getProsecutionConcluded());
    }*/


}

