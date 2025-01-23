package uk.gov.justice.laa.crime.crowncourt.reports.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;
import uk.gov.justice.laa.crime.crowncourt.repository.DeadLetterMessageRepository;
import uk.gov.justice.laa.crime.crowncourt.util.GenerateCsvUtil;
import uk.gov.service.notify.NotificationClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeadLetterMessageReportService {
  
  @Setter
  @Value("${emailClient.notify.dropped_prosecution.template-id}")
  private String templateId;

  @Setter
  @Value("#{'${emailClient.notify.dropped_prosecution.recipient}'.split(',')}")
  private List<String> emailAddresses;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  
  private static final String FILE_NAME_TEMPLATE = "Dropped_Prosecution_Concluded_Messages_Report_%s-%s";
  private static final String HEADINGS = "MAAT ID, Reason, Received time";
  private static final String PENDING = "PENDING";
  private static final String PROCESSED = "PROCESSED";
  private final DeadLetterMessageRepository deadLetterMessageRepository;
  private final EmailNotificationService emailNotificationService;
  
  public void generateReport() throws IOException, NotificationClientException {
    
    List<String> reportContents = generateReportContents();
    
    if (reportContents.isEmpty()) {
      log.info("No dead letter messages found on {}", LocalDate.now());
      return;
    }

    DateTimeFormatter dateRangeFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy");
    String fileName = String.format(FILE_NAME_TEMPLATE, startTime.format(dateRangeFormatter), 
        endTime.format(dateRangeFormatter));
    File reportFile = outputToFile(fileName, reportContents);

    emailNotificationService.send(templateId, emailAddresses, reportFile, fileName);
    // Update reporting status with PROCESSED for reported cases back to business
    updateReportStatus();
    log.info(String.valueOf(reportFile.toPath()));
    Files.delete(reportFile.toPath());
  }
  
  public List<String> generateReportContents() {
    Sort sort = Sort.by("deadLetterReason").ascending().and(Sort.by("receivedTime").descending());
    List<DeadLetterMessageEntity> deadLetterMessageList = deadLetterMessageRepository.findByReportingStatus(PENDING, sort);

    if (CollectionUtils.isEmpty(deadLetterMessageList)) {
      log.info("No dead letter messages found on {}", LocalDate.now());
    } else {
      return prepareLinesForCsv(deadLetterMessageList);
    }
    
    return Collections.emptyList();
  }
  
  public File outputToFile(String fileName, List<String> lines) throws IOException {
    File reportFile = GenerateCsvUtil.generateCsvFile(lines, fileName);
    log.info("CSV file is generated for dead letter messages - {}", fileName);
    return reportFile;
  }

  private List<String> prepareLinesForCsv(List<DeadLetterMessageEntity> deadLetterMessageList) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    List<String> lines = new ArrayList<>();
    
    lines.add(HEADINGS);

    lines.addAll(deadLetterMessageList.stream()
        .map(deadLetterMessage -> deadLetterMessage.getMessage().getMaatId() + ","
            + deadLetterMessage.getDeadLetterReason() + ","
            + deadLetterMessage.getReceivedTime().format(dateFormatter))
        .toList());
    
    // Append a blank line between the list and summary
    lines.add(System.lineSeparator());
    
    // Append the summary to the end
    List<String> summary = generateSummary(deadLetterMessageList);
    lines.addAll(summary);
    
    return lines;
  }
  
  private List<String> generateSummary(List<DeadLetterMessageEntity> deadLetterMessageList) {
    long totalCount = deadLetterMessageList.size();

    if (CollectionUtils.isEmpty(deadLetterMessageList)) {
      return Collections.emptyList();
    }
    
    // Get the timestamp of the first dead letter message
    Optional<LocalDateTime> optionalStartTime = deadLetterMessageList.stream()
        .map(DeadLetterMessageEntity::getReceivedTime)
        .reduce((timestamp1, timestamp2) -> timestamp1.isBefore(timestamp2) ? timestamp1 : timestamp2);

    optionalStartTime.ifPresent(localDateTime -> startTime = localDateTime);
        
    // Get the timestamp of the last dead letter message
    Optional<LocalDateTime> optionalEndTime = deadLetterMessageList.stream()
        .map(DeadLetterMessageEntity::getReceivedTime)
        .reduce((timestamp1, timestamp2) -> timestamp1.isAfter(timestamp2) ? timestamp1 : timestamp2);

    optionalEndTime.ifPresent(localDateTime -> endTime = localDateTime);
    
    List<String> summary = new ArrayList<>();

    summary.add("Reason for failure,,");
    
    Map<String, Long> reasonCounts = deadLetterMessageList.stream()
        .collect(Collectors.groupingBy(DeadLetterMessageEntity::getDeadLetterReason, Collectors.counting()));

    summary.addAll(reasonCounts.entrySet().stream()
        .map(entry -> {
          String reason = entry.getKey();
          Long count = entry.getValue();
          Double percentage = (entry.getValue() * 100.0) / totalCount;
          return String.format("%s,%d,%.0f%%", reason, count, percentage);
        })
        .toList());

    summary.add(String.format("(Of Total),%d,", deadLetterMessageList.size()));

    summary.add(System.lineSeparator());

    DateTimeFormatter dateRangeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    summary.add(startTime.format(dateRangeFormatter) + " to " + endTime.format(dateRangeFormatter));
    
    return summary;
  }
  
  private void updateReportStatus() {
    deadLetterMessageRepository.updateReportingStatus(PROCESSED, PENDING);
  }
}
