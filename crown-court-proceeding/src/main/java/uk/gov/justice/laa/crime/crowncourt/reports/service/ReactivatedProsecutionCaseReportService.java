package uk.gov.justice.laa.crime.crowncourt.reports.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCaseEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAPIService;
import uk.gov.justice.laa.crime.crowncourt.repository.ReactivatedProsecutionCaseRepository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.ReportingStatus;

@Service
@RequiredArgsConstructor
public class ReactivatedProsecutionCaseReportService implements ReportService {

  private final CourtDataAPIService courtDataAPIService;
  private final ReactivatedProsecutionCaseRepository reactivatedProsecutionCaseRepository;
  private final EmailNotificationService emailNotificationService;

  @Override
  public void generateReport() {
    var reactivatedProsecutionCases =
        reactivatedProsecutionCaseRepository.getByReportingStatus(ReportingStatus.PENDING);
    var prosecutionCasesToReport = processReactivatedCases(reactivatedProsecutionCases);
    String emailBodyContent = generateEmailReportBody(prosecutionCasesToReport);
    emailNotificationService.send(emailBodyContent);
    updateStatusOfReportedCases(prosecutionCasesToReport);
  }

  private void updateStatusOfReportedCases(List<ReactivatedProsecutionCaseEntity> prosecutionCasesToReport) {
    prosecutionCasesToReport.forEach(reactivatedProsecutionCaseEntity -> reactivatedProsecutionCaseEntity.setReportingStatus(ReportingStatus.PROCESSED));
    reactivatedProsecutionCaseRepository.saveAll(prosecutionCasesToReport);
  }

  private List<ReactivatedProsecutionCaseEntity> processReactivatedCases(List<ReactivatedProsecutionCaseEntity> reactivatedProsecutionCases) {
    return reactivatedProsecutionCases.stream().filter(this::confirmReactivatedStatus).toList();
  }

  private boolean confirmReactivatedStatus(ReactivatedProsecutionCaseEntity reactivatedProsecutionCase) {
    // Expand the RepOrderDTO to include the list of crown court outcomes.
    RepOrderDTO repOrderDTO = courtDataAPIService.getRepOrder(reactivatedProsecutionCase.getMaatId());
    return repOrderDTO != null;
  }

  private String generateEmailReportBody(List<ReactivatedProsecutionCaseEntity> reactivatedProsecutionCaseEntitiesToReport) {
    String maatIds = reactivatedProsecutionCaseEntitiesToReport.stream()
        .map(reactivatedProsecutionCaseEntity -> reactivatedProsecutionCaseEntity.getMaatId().toString())
        .collect(Collectors.joining(","));
    return String.format("The following prosecution cases have been reactivated have invalid outcomes. %s", maatIds);
  }
}
