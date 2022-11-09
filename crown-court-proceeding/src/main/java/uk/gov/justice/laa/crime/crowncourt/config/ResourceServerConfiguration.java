package uk.gov.justice.laa.crime.crowncourt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@Order(1)
@EnableWebSecurity
public class ResourceServerConfiguration {

    public static final String SCOPE_READ = "SCOPE_READ";
    public static final String SCOPE_READ_WRITE = "SCOPE_READ_WRITE";

    @Bean
    protected BearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint() {
        BearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();
        bearerTokenAuthenticationEntryPoint.setRealmName("Crown Court Proceedings API");
        return bearerTokenAuthenticationEntryPoint;
    }

    @Bean
    public AccessDeniedHandler bearerTokenAccessDeniedHandler() {
        return new BearerTokenAccessDeniedHandler();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests(auth -> auth
                        .antMatchers("/oauth2/**").permitAll()
                        .antMatchers("/open-api/**").permitAll()
                        .antMatchers("/actuator/**").permitAll()
                        .antMatchers("/error").permitAll()
                        .antMatchers(HttpMethod.GET, "/crown-court-proceeding/**").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/**").hasAnyAuthority(SCOPE_READ, SCOPE_READ_WRITE)
                        .antMatchers(HttpMethod.POST, "/api/**").hasAuthority(SCOPE_READ_WRITE)
                        .antMatchers(HttpMethod.PUT, "/api/**").hasAuthority(SCOPE_READ_WRITE)
                        .antMatchers(HttpMethod.DELETE, "/api/**").hasAuthority(SCOPE_READ_WRITE)
                        .antMatchers(HttpMethod.PATCH, "/api/**").hasAuthority(SCOPE_READ_WRITE)
                        .anyRequest().authenticated())
                .oauth2ResourceServer().accessDeniedHandler(bearerTokenAccessDeniedHandler()).authenticationEntryPoint(bearerTokenAuthenticationEntryPoint())
                .jwt()
        ;

        return http.build();
    }
}
