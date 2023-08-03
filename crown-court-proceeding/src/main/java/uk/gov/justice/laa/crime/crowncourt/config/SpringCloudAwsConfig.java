package uk.gov.justice.laa.crime.crowncourt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;


@Configuration
@RequiredArgsConstructor
public class SpringCloudAwsConfig {
    private final SqsProperties sqsProperties;

    @Bean
    public MappingJackson2MessageConverter createMappingJackson2MessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        MappingJackson2MessageConverter messageConverter =
                new MappingJackson2MessageConverter();
        messageConverter.setSerializedPayloadClass(String.class);
        messageConverter.setObjectMapper(objectMapper);
        return messageConverter;
    }

}