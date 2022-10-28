package uk.gov.justice.laa.crime.crowncourt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties(prefix = "retry-config")
public class RetryConfiguration {
    @NotNull
    private Integer maxRetries;

    @NotNull
    private Integer minBackOffPeriod;

    @NotNull
    private Double jitterValue;
}
