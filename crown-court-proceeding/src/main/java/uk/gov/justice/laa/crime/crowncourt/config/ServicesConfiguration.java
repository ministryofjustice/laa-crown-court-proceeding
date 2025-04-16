package uk.gov.justice.laa.crime.crowncourt.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotNull;

@Data
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "services")
public class ServicesConfiguration {

    @NotNull
    private MaatApi maatApi;

    @NotNull
    private CourtDataAdapter courtDataAdapter;

    @NotNull
    private Evidence  evidence;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MaatApi {
        /**
         * The API's Base URL
         */
        @NotNull
        private String baseUrl;

        @NotNull
        private String registrationId;
        
        /**
         * Determines whether oAuth authentication is enabled
         */
        @NotNull
        private boolean oAuthEnabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourtDataAdapter {

        @NotNull
        private String baseUrl;

        @NotNull
        private String registrationId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Evidence {

        @NotNull
        private String baseUrl;

        @NotNull
        private String registrationId;
    }
}
