package uk.gov.justice.laa.crime.crowncourt.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cloud-platform.aws.sqs")
public class SqsProperties {
    private String region;
    private String accessKey;
    private String secretKey;
}
