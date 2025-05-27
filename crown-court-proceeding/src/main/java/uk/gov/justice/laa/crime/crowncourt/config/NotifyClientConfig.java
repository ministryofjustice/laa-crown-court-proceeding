package uk.gov.justice.laa.crime.crowncourt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class NotifyClientConfig {

    @Value("${emailClient.notify.key}")
    private String apiKey;

    @Bean
    public NotificationClient notificationClient() {
        return new NotificationClient(apiKey);
    }
}
