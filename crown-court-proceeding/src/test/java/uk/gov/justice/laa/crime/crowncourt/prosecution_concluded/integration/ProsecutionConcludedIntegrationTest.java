package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHeaders;
import uk.gov.justice.laa.crime.crowncourt.data.builder.TestModelDataBuilder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener.ProsecutionConcludedListener;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.PleaTrialOutcome;

import java.util.HashMap;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SoftAssertionsExtension.class)
class ProsecutionConcludedIntegrationTest {

    @InjectSoftAssertions
    private SoftAssertions softly;
    @Mock
    private Gson gson;
    @InjectMocks
    private ProsecutionConcludedListener prosecutionConcludedListener;
    @Mock
    private QueueMessageLogService queueMessageLogService;

    private static MockWebServer mockMaatCourtDataApi;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void givenJSONMessageIsReceived_whenProsecutionConcludedListenerIsInvoked_thenReceiveIsCalled() throws JsonProcessingException {
        String message = getSqsMessagePayload();
        Gson locaGson = new Gson();
        ProsecutionConcluded prosecutionConcluded = locaGson.fromJson(getSqsMessagePayload(), ProsecutionConcluded.class);
        String originatingHearingId = "61600a90-89e2-4717-aa9b-a01fc66130c1";


        mockMaatCourtDataApi.enqueue(new MockResponse()
                .setResponseCode(OK.code())
                .setHeader("Content-Type", MediaType.APPLICATION_JSON)
                //.setBody(objectMapper.writeValueAsString(TestModelDataBuilder.getIOJAppealDTO()))
        );

        prosecutionConcludedListener.receive(message, new MessageHeaders(new HashMap<>()));

        //then
        verify(queueMessageLogService).createLog(MessageType.PROSECUTION_CONCLUDED, message);

        softly.assertThat(prosecutionConcluded.getProsecutionCaseId()).hasToString("998984a0-ae53-466c-9c13-e0c84c1fd581");
        softly.assertThat(prosecutionConcluded.isConcluded()).isTrue();
        softly.assertThat(prosecutionConcluded.getDefendantId()).hasToString("aa07e234-7e80-4be1-a076-5ab8a8f49df5");
        softly.assertThat(prosecutionConcluded.getHearingIdWhereChangeOccurred()).hasToString(originatingHearingId);
        softly.assertThat(prosecutionConcluded.getOffenceSummary()).hasSize(1);

        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getOffenceId()).hasToString("ed0e9d59-cc1c-4869-8fcd-464caf770744");
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getOffenceCode()).isEqualTo("PT00011");
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).isProceedingsConcluded()).isTrue();
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getProceedingsConcludedChangedDate()).isEqualTo("2022-02-01");

        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getPlea().getValue()).isEqualTo(PleaTrialOutcome.GUILTY.name());
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getPlea().getOriginatingHearingId()).hasToString(originatingHearingId);
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getPlea().getPleaDate()).isEqualTo("2022-02-01");

        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getVerdict().getVerdictType().getCategoryType()).isEqualTo(PleaTrialOutcome.GUILTY.name());
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getVerdict().getVerdictType().getCategory()).isEqualTo(PleaTrialOutcome.GUILTY.name());
        softly.assertThat(prosecutionConcluded.getOffenceSummary().get(0).getVerdict().getVerdictType().getSequence()).isEqualTo(4126);

        softly.assertThat(prosecutionConcluded.getMetadata().getLaaTransactionId()).isEqualTo(originatingHearingId);
        softly.assertAll();
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
                               }
                           }
                       ],
                       maatId: 6039349,
                       metadata: {
                           laaTransactionId: 61600a90-89e2-4717-aa9b-a01fc66130c1
                       }
                   }""";
    }
}