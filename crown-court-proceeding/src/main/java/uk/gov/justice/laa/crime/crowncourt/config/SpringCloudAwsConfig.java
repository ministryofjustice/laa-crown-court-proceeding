package uk.gov.justice.laa.crime.crowncourt.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener.ProsecutionConcludedListener;


@Configuration
@RequiredArgsConstructor
public class SpringCloudAwsConfig {
    private final SqsProperties sqsProperties;

//    @Bean
//    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSQS) {
//        return new QueueMessagingTemplate(amazonSQS);
//    }

    @Bean
    public MappingJackson2MessageConverter createMappingJackson2MessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        MappingJackson2MessageConverter messageConverter =
                new MappingJackson2MessageConverter();
        messageConverter.setSerializedPayloadClass(String.class);
        messageConverter.setObjectMapper(objectMapper);
        return messageConverter;
    }

//    @Bean
//    @Primary
//    public AmazonSQSAsync amazonSQSAsync() {
//        return AmazonSQSAsyncClientBuilder
//                .standard()
//                .withRegion(Regions.fromName(sqsProperties.getRegion()))
//                .withCredentials(
//                        new AWSStaticCredentialsProvider(
//                                new BasicAWSCredentials(sqsProperties.getAccessKey(), sqsProperties.getSecretKey())
//                        )
//                )
//                .build();
//    }

//    @Bean
//    public ProsecutionConcludedListener listener() {
//        return new ProsecutionConcludedListener();
//    }
}