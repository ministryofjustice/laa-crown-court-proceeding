package uk.gov.justice.laa.crime.crowncourt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.crime.crowncourt.reports.service.ReactivatedProsecutionCaseReportService;
import uk.gov.service.notify.NotificationClientException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/v1/send-report")
@Tag(
        name = "Reactivated Prosecution Case Report",
        description = "Rest API for Reactivated Prosecution Case Report.")
public class ReactivatedProsecutionCaseReportController {

    private final ReactivatedProsecutionCaseReportService reactivatedProsecutionCaseReportService;

    @GetMapping
    @Operation(description = "Generate Reactivated Prosecution Case Report and send it by email")
    @ApiResponse(responseCode = "200")
    public void sendReport() throws NotificationClientException, IOException {
        reactivatedProsecutionCaseReportService.generateReport();
    }
}
