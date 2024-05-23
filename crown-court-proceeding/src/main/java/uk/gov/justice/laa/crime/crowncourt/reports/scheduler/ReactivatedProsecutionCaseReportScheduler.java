package uk.gov.justice.laa.crime.crowncourt.reports.scheduler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.justice.laa.crime.crowncourt.reports.service.ReactivatedProsecutionCaseReportService;

/**
 * This scheduler orchestrates the generation and delivery of reports for the Prosecution Concluded
 * status updates.
 */
@Slf4j
@Getter
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ReactivatedProsecutionCaseReportScheduler {

    private final ReactivatedProsecutionCaseReportService reactivatedProsecutionCaseReportService;

    @Scheduled(cron = "${reports.reactivated_cases.cron.expression}")
    public void process() {
        reactivatedProsecutionCaseReportService.generateReport();
    }

}
