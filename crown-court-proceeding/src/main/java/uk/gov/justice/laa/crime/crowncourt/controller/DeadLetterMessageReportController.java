package uk.gov.justice.laa.crime.crowncourt.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.crime.crowncourt.reports.service.DeadLetterMessageReportService;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/send-dead-letter-report")
@Tag(name = "Dead Letter Message Report", description = "Rest API for Dead Letter Message Report.")
public class DeadLetterMessageReportController {

  private final DeadLetterMessageReportService deadLetterMessageService;

  @GetMapping
  @Operation(description = "Generate Dead Letter Message Report and send it by email")
  @ApiResponse(responseCode = "200")
  public void sendReport() throws NotificationClientException, IOException {
    log.info("Sending Dead Letter Message Report and send it by email");
    deadLetterMessageService.generateReport();
  }
}