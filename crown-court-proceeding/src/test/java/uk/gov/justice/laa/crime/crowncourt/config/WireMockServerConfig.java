package uk.gov.justice.laa.crime.crowncourt.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@TestConfiguration
public class WireMockServerConfig {

    @Bean
    public WireMockServer webServer() {
        WireMockServer wireMockServer = new WireMockServer(options().port(9999));
        wireMockServer.start();
        return wireMockServer;
    }

    @Bean
    public WebClient webClient(WireMockServer webServer) {
        return WebClient.builder().baseUrl(webServer.baseUrl()).build();
    }
}
