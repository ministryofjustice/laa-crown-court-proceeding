package uk.gov.justice.laa.crime.crowncourt.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrimeEvidenceClient extends RestAPIClient {

    private static final String REGISTERED_ID = "evidence-api";

    @Qualifier("evidenceOAuth2WebClient")
    private final WebClient webClient;

    @Override
    protected WebClient getWebClient() {
        return this.webClient;
    }

    @Override
    protected String getRegistrationId() {
        return REGISTERED_ID;
    }
}