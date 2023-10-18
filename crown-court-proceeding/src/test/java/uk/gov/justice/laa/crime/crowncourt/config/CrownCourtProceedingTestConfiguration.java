package uk.gov.justice.laa.crime.crowncourt.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

@TestConfiguration
@ComponentScan(basePackages = {"uk.gov.justice.laa.crime.commons.tracing"})
public class CrownCourtProceedingTestConfiguration {

    static final String SUB = "sub";
    static final String AUTH0_TOKEN = "token";
    static final String AUTH_ID = "4jefq4i3331tf3d850ve49qofm";

    @Bean
    public JwtDecoder jwtDecoder() {

        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) {
                return jwt();
            }
        };
    }

    public Jwt jwt() {

        Map<String, Object> claims = Map.of(
                SUB, AUTH_ID,
                "scope", "ccp/standard"
        );

        return new Jwt(
                AUTH0_TOKEN,
                Instant.now(),
                Instant.now().plusSeconds(30),
                Map.of("alg", "none"),
                claims
        );
    }
}
