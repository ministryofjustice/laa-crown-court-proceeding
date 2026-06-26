package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator.OU_CODE_IS_MISSING;

import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.validator.ProsecutionConcludedValidator;
import uk.gov.justice.laa.crime.crowncourt.service.DeadLetterMessageService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.PleaTrialOutcome;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.HashMap;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class ProsecutionConcludedListenerHelperTest {

    @InjectSoftAssertions
    private SoftAssertions softly;

    @Mock
    private Gson gson;

    @InjectMocks
    private ProsecutionConcludedListenerHelper prosecutionConcludedListenerHelper;

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
        ProsecutionConcluded prosecutionConcluded =
                locaGson.fromJson(getSqsMessagePayload(), ProsecutionConcluded.class);
        String originatingHearingId = "61600a90-89e2-4717-aa9b-a01fc66130c1";

        when(gson.fromJson(message, ProsecutionConcluded.class)).thenReturn(prosecutionConcluded);
        prosecutionConcludedListenerHelper.receive(message, new MessageHeaders(new HashMap<>()));

        verify(prosecutionConcludedService).execute(prosecutionConcluded);
        verify(queueMessageLogService).createLog(MessageType.PROSECUTION_CONCLUDED, message);

        softly.assertThat(prosecutionConcluded.getProsecutionCaseId())
                .hasToString("998984a0-ae53-466c-9c13-e0c84c1fd581");
        softly.assertThat(prosecutionConcluded.isConcluded()).isTrue();
        softly.assertThat(prosecutionConcluded.getDefendantId()).hasToString("aa07e234-7e80-4be1-a076-5ab8a8f49df5");
        softly.assertThat(prosecutionConcluded.getHearingIdWhereChangeOccurred())
                .hasToString(originatingHearingId);
        softly.assertThat(prosecutionConcluded.getOffenceSummary()).hasSize(1);

        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getOffenceId())
                .hasToString("ed0e9d59-cc1c-4869-8fcd-464caf770744");
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getOffenceCode())
                .isEqualTo("PT00011");
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).isProceedingsConcluded())
                .isTrue();
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getProceedingsConcludedChangedDate())
                .isEqualTo("2022-02-01");

        softly.assertThat(prosecutionConcluded
                        .getOffenceSummary()
                        .get(0)
                        .getPlea()
                        .getValue())
                .isEqualTo(PleaTrialOutcome.GUILTY.name());
        softly.assertThat(prosecutionConcluded
                        .getOffenceSummary()
                        .get(0)
                        .getPlea()
                        .getOriginatingHearingId())
                .hasToString(originatingHearingId);
        softly.assertThat(prosecutionConcluded
                        .getOffenceSummary()
                        .get(0)
                        .getPlea()
                        .getPleaDate())
                .isEqualTo("2022-02-01");

        softly.assertThat(prosecutionConcluded
                        .getOffenceSummary()
                        .get(0)
                        .getVerdict()
                        .getVerdictType()
                        .getCategoryType())
                .isEqualTo(PleaTrialOutcome.GUILTY.name());
        softly.assertThat(prosecutionConcluded
                        .getOffenceSummary()
                        .get(0)
                        .getVerdict()
                        .getVerdictType()
                        .getCategory())
                .isEqualTo(PleaTrialOutcome.GUILTY.name());
        softly.assertThat(prosecutionConcluded
                        .getOffenceSummary()
                        .get(0)
                        .getVerdict()
                        .getVerdictType()
                        .getSequence())
                .isEqualTo(4126);

        softly.assertThat(prosecutionConcluded.getMetadata().getLaaTransactionId())
                .isEqualTo(originatingHearingId);

        softly.assertAll();
    }

    @Test
    void givenInvalidMessageWithMissingMaatId_whenExecuteIsInvoked_thenShouldNotCallService() {
        // given - a message with missing maatId
        String message = getSqsMessagePayload();
        JsonObject msgObject = JsonParser.parseString(message).getAsJsonObject();
        msgObject.remove("maatId");
        message = new Gson().toJson(msgObject);
        // and - the validator throws an exception
        doThrow(new ValidationException(ProsecutionConcludedValidator.MAAT_ID_FORMAT_INCORRECT))
                .when(prosecutionConcludedValidator)
                .getAndValidateMaatId(any());

        // when - execute is invoked
        prosecutionConcludedListenerHelper.receive(message, new MessageHeaders(new HashMap<>()));

        // then - should log the message
        verify(queueMessageLogService, times(1)).createLog(MessageType.PROSECUTION_CONCLUDED, message);
        // then - should not call service
        verify(prosecutionConcludedService, never()).execute(any());
        // then - should not copy message to dead letter table
        verify(deadLetterMessageService, never()).logDeadLetterMessage(any(), any());
    }

    @Test
    void givenAValidationExceptionDuringProcessing_whenExecuteIsInvoked_thenShouldLogDeadLetterMessage() {
        // given - a message with missing maatId
        String message = getSqsMessagePayload();
        // and - the service throws a validation exception
        doThrow(new ValidationException(OU_CODE_IS_MISSING))
                .when(prosecutionConcludedService)
                .execute(any());

        // when - execute is invoked
        prosecutionConcludedListenerHelper.receive(message, new MessageHeaders(new HashMap<>()));

        // then - should log the message
        verify(queueMessageLogService, times(1)).createLog(MessageType.PROSECUTION_CONCLUDED, message);
        // then - should copy message to dead letter table
        verify(deadLetterMessageService, times(1)).logDeadLetterMessage(Mockito.eq(OU_CODE_IS_MISSING), any());
    }

    @Test
    void givenAnUnexpectedExceptionDuringProcessing_whenExecuteIsInvoked_thenRethrowAsARuntimeException() {
        // given - a message with missing maatId
        String message = getSqsMessagePayload();
        // and - the service throws a validation exception
        doThrow(new RuntimeException("Something bad happened"))
                .when(prosecutionConcludedService)
                .execute(any());

        // when - execute is invoked
        RuntimeException runtimeException = assertThrows(
                RuntimeException.class,
                () -> prosecutionConcludedListenerHelper.receive(message, new MessageHeaders(new HashMap<>())));

        // then - should log the message
        verify(queueMessageLogService, times(1)).createLog(MessageType.PROSECUTION_CONCLUDED, message);
        // and - runtime exception includes actual exception as the cause
        assertThat(runtimeException.getCause().getMessage()).isEqualTo("Something bad happened");
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
