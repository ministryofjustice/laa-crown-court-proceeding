package uk.gov.justice.laa.crime.crowncourt.reports.scheduler;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import uk.gov.justice.laa.crime.crowncourt.reports.service.ReactivatedProsecutionCaseReportService;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReactivatedProsecutionCaseReportSchedulerTest {

    @InjectMocks
    private ReactivatedProsecutionCaseReportScheduler reactiveProsecutionCaseReportScheduler;

    @Mock
    private ReactivatedProsecutionCaseReportService reactiveProsecutionCaseReportService;

    @Test
    void testReactivatedProsecutionCaseReport() throws NotificationClientException, IOException {
        doNothing().when(reactiveProsecutionCaseReportService).generateReport();
        reactiveProsecutionCaseReportScheduler.process();
        verify(reactiveProsecutionCaseReportService).generateReport();
    }
}
