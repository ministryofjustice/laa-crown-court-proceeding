package uk.gov.justice.laa.crime.crowncourt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties(prefix = "features")
public class FeaturesConfiguration {

    @NotNull
    private boolean dateCompletionEnabled;
}
