package integration;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.EnableWireMock;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;

@EnableWireMock
@DirtiesContext
@AutoConfigureObservability
@SpringBootTest(classes = {CrownCourtProceedingApplication.class})
@Testcontainers
class ProsecutionConcludedOutcomeTest extends AbstractProsecutionConcludedTest {

    private static final String AQUITTED = "AQUITTED";
    private static final String CONVICTED = "CONVICTED";
    private static final String PART_CONVICTED = "PART CONVICTED";
    private static final String SUCCESSFUL = "SUCCESSFUL";
    private static final String CC_OUTCOME_URL = "/api/internal/v1/assessment/crown-court/updateCCOutcome";

    @Test
    void givenPleaIsGuiltyAndNoVerdict_whenListenerIsInvoked_thenOutcomeIsConvicted() {
        // given - the message has 1 offence: Plea = GUILTY, Verdict = None
        String message = getMessageFromFile("SqsPayloadPleaWithGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to CONVICTED
        thenTheOutcomeIs(CONVICTED);
    }

    @Test
    void givenAPleaNotGuiltyAndNoVerdict_whenListenerIsInvoked_thenOutcomeIsAquitted() {
        // given - the message has 1 offence: Plea = NOT GUILTY, Verdict = None
        String message = getMessageFromFile("SqsPayloadPleaWithNotGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to AQUITTED
        thenTheOutcomeIs(AQUITTED);
    }

    @Test
    void givenAVerdictIsGuiltyAndNoPlea_whenListenerIsInvoked_thenOutcomeIsAConvicted() {
        // given - the message has 1 offence: Plea = None, Verdict = GUILTY
        String message = getMessageFromFile("SqsPayloadVerdictWithGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to CONVICTED
        thenTheOutcomeIs(CONVICTED);
    }

    @Test
    void givenAVerdictIsNotGuiltyAndNoPlea_whenListenerIsInvoked_thenOutcomeIsAquitted() {
        // given - the message has 1 offence: Plea = None, Verdict = NOT GUILTY
        String message = getMessageFromFile("SqsPayloadVerdictWithNotGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to AQUITTED
        thenTheOutcomeIs(AQUITTED);
    }

    @Test
    void givenAPleaIsGuiltyAndVerdictIsGuilty_whenListenerIsInvoked_thenOutcomeIsConvicted() {
        // given - the message has 1 offence: Plea = GUILTY, Verdict = GUILTY
        String message = getMessageFromFile("SqsPayloadPleaAndVerdictWithGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to CONVICTED
        thenTheOutcomeIs(CONVICTED);
    }

    @Test
    void givenAPleaIsNotGuiltyAndVerdictIsNotGuilty_whenListenerIsInvoked_thenOutcomeIsAquitted() {
        // given - the message has 1 offence: Plea = NOT GUILTY, Verdict = NOT GUILTY
        String message = getMessageFromFile("SqsPayloadPleaAndVerdictWithNotGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to AQUITTED
        thenTheOutcomeIs(AQUITTED);
    }

    @Test
    void givenAPleaIsNotGuiltyAndVerdictIsGuilty_whenListenerIsInvoked_thenOutcomeIsConvicted() {
        // given - the message has 1 offence: Plea = NOT GUILTY, Verdict = GUILTY
        String message = getMessageFromFile("SqsPayloadPleaIsNotGuiltyAndVerdictIsGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to CONVICTED
        thenTheOutcomeIs(CONVICTED);
    }

    @Test
    void givenAMultipleOffenceWithPleaAndNoVerdict_whenListenerIsInvoked_thenOutcomeIsAConvicted() {
        // given - the message has multiple offences:
        // 1. Plea = GUILTY, Verdict = None
        // 2. Plea = GUILTY, Verdict = None
        String message = getMessageFromFile("SqsMultipleOffenceWithPleaIsGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to CONVICTED
        thenTheOutcomeIs(CONVICTED);
    }

    @Test
    void givenAMultipleOffence_whenPleaIsGuiltyAndVerdictIsNotGuilty_thenOutcomeIsPartConvicted() {
        // given - the message has multiple offences:
        // 1. Plea = GUILTY, Verdict = None
        // 2. Plea = GUILTY, Verdict = NOT GUILTY
        String message = getMessageFromFile("SqsMultipleOffenceWithPleaAndVerdictIsNotGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to PART_CONVICTED
        thenTheOutcomeIs(PART_CONVICTED);
    }

    @Test
    void givenAMultipleOffence_withPleaIsNotGuiltyAndVerdictIsNotGuilty_thenOutcomeIsAquitted() {
        // given - the message has multiple offences:
        // 1. Plea = NOT GUILTY, Verdict = None
        // 2. Plea = NOT GUILTY, Verdict = NOT GUILTY
        String message = getMessageFromFile("SqsMultipleOffenceWithNotGuilty.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to AQUITTED
        thenTheOutcomeIs(AQUITTED);
    }

    @Test
    void givenAMultipleOffence_withNoPleaAndNoVerdictInformation_thenOutcomeIsAquitted() {
        // given - the message has multiple offences:
        // 1. Plea = NOT GUILTY, Verdict = None
        // 2. Plea = None, Verdict = None
        String message = getMessageFromFile("SqsMultipleOffenceWithNoPleaAndNoVerdict.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to AQUITTED
        thenTheOutcomeIs(AQUITTED);
    }

    @Test
    void givenAMultipleOffence_withNoPleaNoVerdictInformation_thenOutcomeIsAquitted() {
        // given - the message has multiple offences:
        // 1. Plea = NOT GUILTY, Verdict = None
        // 2. Plea = None, Verdict = None, result is present, isConvictedResult = true
        // TODO - Check: this JSON contains a "result" field for the offence which is not in the OffenceSummary class
        String message = getMessageFromFile("SqsMultipleOffenceWithNoPleaNoVerdictAndIsConvictedResult.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to AQUITTED
        thenTheOutcomeIs(AQUITTED);
    }

    @Test
    void givenAMultipleOffence_withNoPleaNoVerdictInformationAndNotIsConvictedResult_thenOutcomeIsAquitted() {
        // given - the message has multiple offences:
        // 1. Plea = NOT GUILTY, Verdict = None
        // 2. Plea = None, Verdict = None, result is present, isConvictedResult = false
        // TODO - Check: this JSON contains a "result" field for the offence which is not in the OffenceSummary class
        String message = getMessageFromFile("SqsMultipleOffenceWithNoPleaNoVerdictAndNotIsConvictedResult.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to AQUITTED
        thenTheOutcomeIs(AQUITTED);
    }

    @Test
    void givenAppealsConcludedResultIsReceived_whenListenerIsInvoked_thenOutcomeIsSuccessful() {
        // given - the message has a single offence: Plea = NOT GUILTY, Verdict = GUILTY and
        // an "applicationConcluded" field (appeals) present
        String message = getMessageFromFile("SqsAppealsPayload.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a request was sent to MAAT Data API to update the outcome to SUCCESSFUL
        thenTheOutcomeIs(SUCCESSFUL);
    }

    private void thenTheOutcomeIs(String outcome) {
        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests).hasSize(1);
        String expectedRequest =
                switch (outcome) {
                    case AQUITTED -> getExpectedRequest(AQUITTED, (Boolean) null);
                    case CONVICTED -> getExpectedRequest(CONVICTED, false);
                    case PART_CONVICTED -> getExpectedRequest(PART_CONVICTED, false);
                    case SUCCESSFUL -> getExpectedAppealsRequest(SUCCESSFUL);
                    default -> throw new IllegalStateException("Unexpected value: " + outcome);
                };
        assertThat(requests.getFirst().getBodyAsString()).isEqualTo(expectedRequest);
    }

    private String getExpectedAppealsRequest(String outcome) {
        return "{\"repId\":6151867,\"ccOutcome\":\"" + outcome
                + "\",\"benchWarrantIssued\":null,\"appealType\":\"ACN\",\"imprisoned\":null,\"caseNumber\":\"21GN1208521\",\"crownCourtCode\":null}";
    }

    private String getExpectedRequest(String outcome, Boolean imprisoned) {
        String imprisonedString = imprisoned == null ? "null" : imprisoned ? "\"Y\"" : "\"N\"";
        return "{\"repId\":5635567,\"ccOutcome\":\"" + outcome
                + "\",\"benchWarrantIssued\":null,\"appealType\":\"ACN\",\"imprisoned\":" + imprisonedString
                + ",\"caseNumber\":\"21GN1208521\",\"crownCourtCode\":\"433\"}";
    }
}
