package uk.gov.justice.laa.crime.crowncourt.reports.scheduler;

import lombok.RequiredArgsConstructor;
import uk.gov.justice.laa.crime.crowncourt.reports.service.ReactivatedProsecutionCaseReportService;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ReactivatedProsecutionCaseReportScheduler {

    private final ReactivatedProsecutionCaseReportService reactivatedProsecutionCaseReportService;

    @Scheduled(cron = "${reports.reactivated_cases.cron.expression}")
    public void process() throws NotificationClientException, IOException {
        reactivatedProsecutionCaseReportService.generateReport();
    }
}
