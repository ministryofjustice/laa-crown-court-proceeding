package uk.gov.justice.laa.crime.crowncourt.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqsClientConfig {
    private final SqsProperties sqsProperties;

    public AmazonSQS awsSqsClient() {
        return AmazonSQSClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(sqsProperties.getAccessKey(), sqsProperties.getSecretKey())))
                .withRegion(Regions.fromName(sqsProperties.getRegion())).build();
    }
}
