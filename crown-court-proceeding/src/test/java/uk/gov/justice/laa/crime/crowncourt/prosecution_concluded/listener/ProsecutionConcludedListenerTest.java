package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener;

import com.google.gson.Gson;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.enums.CallerType;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.PleaTrialOutcome;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class ProsecutionConcludedListenerTest {

    @InjectSoftAssertions
    private SoftAssertions softly;
    @Mock
    private Gson gson;
    @InjectMocks
    private ProsecutionConcludedListener prosecutionConcludedListener;
    @Mock
    private DeadLetterMessageService deadLetterMessageService;
    @Mock
    private ProsecutionConcludedService prosecutionConcludedService;
    @Mock
    private ProsecutionConcludedValidator prosecutionConcludedValidator;
    @Mock
    private QueueMessageLogService queueMessageLogService;

    @Test
    void givenJSONMessageIsReceived_whenProsecutionConcludedListenerIsInvoked_thenReceiveIsCalled() {
        Gson locaGson = new Gson();
        String message = getSqsMessagePayload();
        ProsecutionConcluded prosecutionConcluded = locaGson.fromJson(
                getSqsMessagePayload(), ProsecutionConcluded.class
        );
        String originatingHearingId = "61600a90-89e2-4717-aa9b-a01fc66130c1";

        when(gson.fromJson(message, ProsecutionConcluded.class))
                .thenReturn(prosecutionConcluded);
        prosecutionConcludedListener.receive(message, new MessageHeaders(new HashMap<>()));

        verify(prosecutionConcludedService).execute(prosecutionConcluded, CallerType.QUEUE);
        verify(queueMessageLogService).createLog(MessageType.PROSECUTION_CONCLUDED, message);

        softly.assertThat(prosecutionConcluded.getProsecutionCaseId())
                .hasToString("998984a0-ae53-466c-9c13-e0c84c1fd581");
        softly.assertThat(prosecutionConcluded.isConcluded())
                .isTrue();
        softly.assertThat(prosecutionConcluded.getDefendantId())
                .hasToString("aa07e234-7e80-4be1-a076-5ab8a8f49df5");
        softly.assertThat(prosecutionConcluded.getHearingIdWhereChangeOccurred())
                .hasToString(originatingHearingId);
        softly.assertThat(prosecutionConcluded.getOffenceSummary())
                .hasSize(1);

        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getOffenceId())
                .hasToString("ed0e9d59-cc1c-4869-8fcd-464caf770744");
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getOffenceCode())
                .isEqualTo("PT00011");
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).isProceedingsConcluded())
                .isTrue();
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getProceedingsConcludedChangedDate())
                .isEqualTo("2022-02-01");

        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getPlea().getValue())
                .isEqualTo(PleaTrialOutcome.GUILTY.name());
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getPlea().getOriginatingHearingId())
                .hasToString(originatingHearingId);
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getPlea().getPleaDate())
                .isEqualTo("2022-02-01");

        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getVerdict().getVerdictType().getCategoryType())
                .isEqualTo(PleaTrialOutcome.GUILTY.name());
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getVerdict().getVerdictType().getCategory())
                .isEqualTo(PleaTrialOutcome.GUILTY.name());
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getVerdict().getVerdictType().getSequence())
                .isEqualTo(4126);

        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getResults().get(0).getIsConvictedResult())
                .isEqualTo(true);

        softly.assertThat(prosecutionConcluded.getMetadata().getLaaTransactionId())
                .isEqualTo(originatingHearingId);

        softly.assertAll();
    }


    @Test
    void givenInvalidMessage_whenProsecutionConcludedListenerIsInvoked_thenShouldNotCallService() {
        String message = getSqsMessagePayload();
        ProsecutionConcluded prosecutionConcluded = gson.fromJson(message, ProsecutionConcluded.class);

        doThrow(new ValidationException(ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL)).when(prosecutionConcludedValidator).validateMaatId(any());
        prosecutionConcludedListener.receive(message, new MessageHeaders(new HashMap<>()));
        verify(prosecutionConcludedService, times(0)).execute(any(), any());
        verify(deadLetterMessageService, times(1)).logDeadLetterMessage(
            ProsecutionConcludedValidator.PAYLOAD_IS_NOT_AVAILABLE_OR_NULL, prosecutionConcluded);
    }

    private String getSqsMessagePayload() {
        return """
                {
                   prosecutionCaseId : 998984a0-ae53-466c-9c13-e0c84c1fd581,
                   defendantId: aa07e234-7e80-4be1-a076-5ab8a8f49df5,
                   isConcluded: true,
                   hearingIdWhereChangeOccurred : 61600a90-89e2-4717-aa9b-a01fc66130c1,
                   offenceSummary: [
                           {
                               offenceId: ed0e9d59-cc1c-4869-8fcd-464caf770744,
                               offenceCode: PT00011,
                               proceedingsConcluded: true,
                               proceedingsConcludedChangedDate: 2022-02-01,
                               plea: {
                                   originatingHearingId: 61600a90-89e2-4717-aa9b-a01fc66130c1,
                                   value: GUILTY,
                                   pleaDate: 2022-02-01
                               },
                               verdict: {
                                   verdictDate: 2022-02-01,
                                   originatingHearingId: 61600a90-89e2-4717-aa9b-a01fc66130c1,
                                   verdictType: {
                                       description: GUILTY,
                                       category: GUILTY,
                                       categoryType: GUILTY,
                                       sequence: 4126,
                                       verdictTypeId: null
                                   }
                               },
                               "results":
                                   [
                                       {
                                           "resultCode": "1017",
                                           "resultShortTitle": "Absolute discharge",
                                           "resultText": "AD - Absolute discharge",
                                           "category": "FINAL",
                                           "resultCodeQualifiers": null,
                                           "nextHearingDate": null,
                                           "nextHearingLocation": null,
                                           "laaOfficeAccount": null,
                                           "legalAidWithdrawalDate": "2025-04-11",
                                           "isConvictedResult": true
                                       }
                                   ]
                           }
                       ],
                       maatId: 6039349,
                       metadata: {
                           laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                       }
                   }""";
    }
}