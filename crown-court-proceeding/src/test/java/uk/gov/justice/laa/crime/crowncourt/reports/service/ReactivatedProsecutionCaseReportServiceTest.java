package uk.gov.justice.laa.crime.crowncourt.reports.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCase;
import uk.gov.justice.laa.crime.crowncourt.repository.ReactivatedProsecutionCaseRepository;
import uk.gov.justice.laa.crime.crowncourt.util.GenerateCsvUtil;
import uk.gov.service.notify.NotificationClientException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReactivatedProsecutionCaseReportServiceTest {

    private static final String PENDING = "PENDING";
    private static final String PROCESSED = "PROCESSED";

    @Mock
    private ReactivatedProsecutionCaseRepository reactivatedProsecutionCaseRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private ReactivatedProsecutionCaseReportService reactivatedProsecutionCaseReportService;

    @BeforeEach
    public void setUp() {
        reactivatedProsecutionCaseReportService.setTemplateId("test-template-id");
        reactivatedProsecutionCaseReportService.setEmailAddresses(List.of("test1@example.com", "test2@example.com"));
    }

    @Test
    void testGenerateReport_NoReactivatedCasesFound() throws IOException, NotificationClientException {

        when(reactivatedProsecutionCaseRepository.findByReportingStatus(PENDING))
                .thenReturn(Collections.emptyList());

        reactivatedProsecutionCaseReportService.generateReport();

        verify(reactivatedProsecutionCaseRepository, times(1)).findByReportingStatus(PENDING);
        verify(emailNotificationService, never()).send(anyString(), anyList(), any(File.class), anyString());
    }

    @Test
    void testGenerateReport_ReactivatedCasesFound() throws IOException, NotificationClientException {

        List<ReactivatedProsecutionCase> reactivatedCases = List.of(new ReactivatedProsecutionCase());
        File mockFile = Files.createTempFile("temp", ".csv").toFile();
        when(reactivatedProsecutionCaseRepository.findByReportingStatus(PENDING))
                .thenReturn(reactivatedCases);

        reactivatedProsecutionCaseReportService.generateReport();

        verify(reactivatedProsecutionCaseRepository, times(1)).findByReportingStatus(PENDING);
        verify(emailNotificationService, times(1)).send(anyString(), anyList(), any(File.class), anyString());
        verify(reactivatedProsecutionCaseRepository, times(1)).updateReportingStatus(PROCESSED, PENDING);

        Files.deleteIfExists(mockFile.toPath());
    }

    @Test
    void testGenerateReport_ExceptionDuringFileGeneration() throws IOException, NotificationClientException {

        List<ReactivatedProsecutionCase> reactivatedCases = List.of(new ReactivatedProsecutionCase());
        when(reactivatedProsecutionCaseRepository.findByReportingStatus(PENDING))
                .thenReturn(reactivatedCases);

        try (MockedStatic<GenerateCsvUtil> mockedGenerateCsvUtil = Mockito.mockStatic(GenerateCsvUtil.class)) {
            mockedGenerateCsvUtil
                    .when(() -> GenerateCsvUtil.generateCsvFile(anyList(), anyString()))
                    .thenThrow(new IOException("File generation failed"));
            assertThrows(IOException.class, () -> reactivatedProsecutionCaseReportService.generateReport());
        }

        verify(reactivatedProsecutionCaseRepository, times(1)).findByReportingStatus(PENDING);
        verify(emailNotificationService, never()).send(anyString(), anyList(), any(File.class), anyString());
        verify(reactivatedProsecutionCaseRepository, never()).updateReportingStatus(PROCESSED, PENDING);
    }

    @Test
    void testGenerateReport_ExceptionDuringEmailSending() throws IOException, NotificationClientException {

        List<ReactivatedProsecutionCase> reactivatedCases = List.of(new ReactivatedProsecutionCase());
        when(reactivatedProsecutionCaseRepository.findByReportingStatus(PENDING))
                .thenReturn(reactivatedCases);
        File mockFile = Files.createTempFile("temp", ".csv").toFile();
        doThrow(new NotificationClientException("Email sending failed"))
                .when(emailNotificationService)
                .send(anyString(), anyList(), any(File.class), anyString());

        assertThrows(NotificationClientException.class, () -> reactivatedProsecutionCaseReportService.generateReport());

        verify(reactivatedProsecutionCaseRepository, times(1)).findByReportingStatus(PENDING);
        verify(emailNotificationService, times(1)).send(anyString(), anyList(), any(File.class), anyString());
        verify(reactivatedProsecutionCaseRepository, never()).updateReportingStatus(PROCESSED, PENDING);

        Files.deleteIfExists(mockFile.toPath());
    }
}
