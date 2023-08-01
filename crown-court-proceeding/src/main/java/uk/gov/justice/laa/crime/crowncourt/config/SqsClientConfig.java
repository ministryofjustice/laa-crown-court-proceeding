//package uk.gov.justice.laa.crime.crowncourt.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.sqs.SqsClient;
//
//@Component
//@RequiredArgsConstructor
//public class SqsClientConfig {
//    private final SqsProperties sqsProperties;
//
//    public SqsClient awsSqsClient() {
//        return SqsClient.builder()
//                .credentialsProvider(
//                        StaticCredentialsProvider.create(
//                                AwsBasicCredentials.create(
//                                        sqsProperties.getAccessKey(), sqsProperties.getSecretKey()
//                                )
//                        )
//                ).region(Region.of(sqsProperties.getRegion())).build();
//    }
//}
