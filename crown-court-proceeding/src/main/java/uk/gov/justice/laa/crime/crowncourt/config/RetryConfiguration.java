package uk.gov.justice.laa.crime.crowncourt.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "retry-config")
public class RetryConfiguration {

    @NotNull
    private Integer maxRetries;

    @NotNull
    private Integer minBackOffPeriod;

    @NotNull
    private Double jitterValue;
}
