package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.scheduler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.justice.laa.crime.crowncourt.service.QueueMessageLogService;

@Slf4j
@Getter
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class PurgeProsecutionConcludedScheduler {

    private final QueueMessageLogService queueMessageLogService;

    @Scheduled(cron = "${queue.message.purge.cron.expression}")
    public void purgePeriodicMessages() {
        log.info("Purge Prosecution concluded Scheduling is started");
        queueMessageLogService.purgePeriodicMessages();
        log.info("Purge process successfully completed.");
    }
}
