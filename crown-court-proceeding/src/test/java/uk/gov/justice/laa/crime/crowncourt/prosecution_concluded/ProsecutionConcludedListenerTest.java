package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.listener.ProsecutionConcludedListener;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedService;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.MessageType;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.PleaTrialOutcome;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProsecutionConcludedListenerTest {

    @Mock
    private Gson gson;
    @InjectMocks
    private ProsecutionConcludedListener prosecutionConcludedListener;
    @Mock
    private ProsecutionConcludedService prosecutionConcludedService;
    @Mock
    private QueueMessageLogService queueMessageLogService;

    @Test
    void givenJSONMessageIsReceived_whenProsecutionConcludedListenerIsInvoked_thenReceiveIsCalled() {
        String message = getSqsMessagePayload();
        Gson locaGson = new Gson();
        ProsecutionConcluded prosecutionConcluded = locaGson.fromJson(getSqsMessagePayload(), ProsecutionConcluded.class);
        String originatingHearingId = "61600a90-89e2-4717-aa9b-a01fc66130c1";

        //when
        when(gson.fromJson(message, ProsecutionConcluded.class)).thenReturn(prosecutionConcluded);
        prosecutionConcludedListener.receive(message);

        //then
        verify(prosecutionConcludedService).execute(prosecutionConcluded);
        verify(queueMessageLogService).createLog(MessageType.PROSECUTION_CONCLUDED, message);

        assertThat(prosecutionConcluded.getProsecutionCaseId()).hasToString("998984a0-ae53-466c-9c13-e0c84c1fd581");
        assertThat(prosecutionConcluded.isConcluded()).isTrue();
        assertEquals("aa07e234-7e80-4be1-a076-5ab8a8f49df5", prosecutionConcluded.getDefendantId().toString());
        assertEquals(originatingHearingId, prosecutionConcluded.getHearingIdWhereChangeOccurred().toString());
        assertEquals(1, prosecutionConcluded.getOffenceSummary().size());

        assertEquals("ed0e9d59-cc1c-4869-8fcd-464caf770744", prosecutionConcluded.getOffenceSummary().get(0).getOffenceId().toString());
        assertEquals("PT00011", prosecutionConcluded.getOffenceSummary().get(0).getOffenceCode());
        assertThat(prosecutionConcluded.getOffenceSummary().get(0).isProceedingsConcluded()).isTrue();
        assertEquals("2022-02-01", prosecutionConcluded.getOffenceSummary().get(0).getProceedingsConcludedChangedDate());

        assertEquals(PleaTrialOutcome.GUILTY.name(), prosecutionConcluded.getOffenceSummary().get(0).getPlea().getValue());
        assertEquals(originatingHearingId, prosecutionConcluded.getOffenceSummary().get(0).getPlea().getOriginatingHearingId().toString());
        assertEquals("2022-02-01", prosecutionConcluded.getOffenceSummary().get(0).getPlea().getPleaDate());

        assertEquals(PleaTrialOutcome.GUILTY.name(), prosecutionConcluded.getOffenceSummary().get(0).getVerdict().getVerdictType().getCategoryType());

        assertEquals(PleaTrialOutcome.GUILTY.name(), prosecutionConcluded.getOffenceSummary().get(0).getVerdict().getVerdictType().getCategory());
        assertEquals(4126, prosecutionConcluded.getOffenceSummary().get(0).getVerdict().getVerdictType().getSequence());

        assertEquals(originatingHearingId, prosecutionConcluded.getMetadata().getLaaTransactionId());
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