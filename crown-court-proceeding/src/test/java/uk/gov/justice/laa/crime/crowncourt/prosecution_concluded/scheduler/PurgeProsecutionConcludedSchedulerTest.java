package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.scheduler;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurgeProsecutionConcludedSchedulerTest {

    @InjectMocks
    private PurgeProsecutionConcludedScheduler scheduler;

    @Mock
    private QueueMessageLogService queueMessageLogService;

    @Test
    void givenAValidInput_whenPurgePeriodicMessagesIsInvoked_thenPeriodicMessages() {
        doNothing().when(queueMessageLogService).purgePeriodicMessages();
        scheduler.purgePeriodicMessages();
        verify(queueMessageLogService).purgePeriodicMessages();
    }
}
