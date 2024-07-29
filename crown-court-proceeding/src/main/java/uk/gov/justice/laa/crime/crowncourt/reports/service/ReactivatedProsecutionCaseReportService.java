package uk.gov.justice.laa.crime.crowncourt.reports.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCase;
import uk.gov.justice.laa.crime.crowncourt.repository.ReactivatedProsecutionCaseRepository;
import uk.gov.justice.laa.crime.crowncourt.util.GenerateCsvUtil;
import uk.gov.service.notify.NotificationClientException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactivatedProsecutionCaseReportService {
    private static final String FILE_NAME_TEMPLATE = "Reactivated_Prosecution_Cases_Report_%s";
    private static final String PENDING = "PENDING";

    private final ReactivatedProsecutionCaseRepository reactivatedProsecutionCaseRepository;
    private final EmailNotificationService emailNotificationService;

    public void generateReport() throws IOException, NotificationClientException {
        List<ReactivatedProsecutionCase> reactivatedCaseList = reactivatedProsecutionCaseRepository.findByReportingStatus(PENDING);
        if (CollectionUtils.isEmpty(reactivatedCaseList)) {
            log.info("No reactivated cases found on {}", LocalDate.now());
        } else {
            String fileName = String.format(FILE_NAME_TEMPLATE, LocalDate.now());
            File reportFile = GenerateCsvUtil.generateCsvFile(reactivatedCaseList, fileName);
            log.info("CSV file is generated for reactivated cases");
            emailNotificationService.send(reportFile, fileName);
        }
    }
}
