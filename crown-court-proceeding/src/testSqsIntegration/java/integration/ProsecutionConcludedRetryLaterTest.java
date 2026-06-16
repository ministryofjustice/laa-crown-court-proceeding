package integration;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CaseConclusionStatus.PENDING;

import uk.gov.justice.laa.crime.crowncourt.CrownCourtProceedingApplication;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;

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
class ProsecutionConcludedRetryLaterTest extends AbstractProsecutionConcludedTest {

    private static final String CC_OUTCOME_URL = "/api/internal/v1/assessment/crown-court/updateCCOutcome";

    @Test
    void givenAValidMessageAndMaatRecordIsLocked_whenListenerIsInvoked_thenShouldCreateWithPending() {
        // given - the MAAT record is locked
        givenTheMaatRecordIsLocked();
        // and - the message is valid
        String message = getMessageFromFile("SqsPayloadPleaAndVerdict.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a record is saved to PROSECUTION_CONCLUDED in PENDING state (to denote it will be retried)
        List<ProsecutionConcludedEntity> list = prosecutionConcludedRepository.getByMaatId(5635566);
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getStatus()).isEqualTo(PENDING.name());

        // and - no outcome was sent to MAAT Data API
        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests).isEmpty();
    }

    @Test
    void givenAValidMessageAndNoHearingData_whenListenerIsInvoked_thenShouldCreateWithPending() {
        // given - the hearing id in the message is not found in MAAT Data API
        String message = getMessageFromFile("SqsPayloadPleaWithHearingNotFound.json");

        // when - the message is processed
        prosecutionConcludedListenerHelper.receive(message, getDefaultMessageHeaders());

        // then - a record is saved to PROSECUTION_CONCLUDED in PENDING state (to denote it will be retried)
        List<ProsecutionConcludedEntity> list = prosecutionConcludedRepository.getByMaatId(5635444);
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getStatus()).isEqualTo(PENDING.name());

        // and - no outcome was sent to MAAT Data API
        List<LoggedRequest> requests = findAll(putRequestedFor(urlEqualTo(CC_OUTCOME_URL)));
        assertThat(requests).isEmpty();
    }
}
