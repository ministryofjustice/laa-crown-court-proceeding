package uk.gov.justice.laa.crime.crowncourt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.UUID;

@Configuration(proxyBeanMethods = false)
public class CrownCourtProceedingTestConfiguration {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    @Primary
    public RegisteredClientRepository registeredTestClientRepository() {

        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("test-client")
                .clientSecret("{noop}secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://127.0.0.1:" + serverPort + "/authorized")
                .scope("READ")
                .scope("READ_WRITE")
                .clientSettings(ClientSettings.builder().build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }
}
