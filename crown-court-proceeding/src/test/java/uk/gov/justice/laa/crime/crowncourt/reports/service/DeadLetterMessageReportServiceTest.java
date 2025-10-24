package uk.gov.justice.laa.crime.crowncourt.reports.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;
import uk.gov.justice.laa.crime.crowncourt.repository.DeadLetterMessageRepository;
import uk.gov.justice.laa.crime.crowncourt.util.GenerateCsvUtil;
import uk.gov.service.notify.NotificationClientException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
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
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class DeadLetterMessageReportServiceTest {

    private static final String PENDING = "PENDING";
    private static final String PROCESSED = "PROCESSED";

    @Mock
    private DeadLetterMessageRepository deadLetterMessageRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private DeadLetterMessageReportService deadLetterMessageReportService;

    public static final Sort DEAD_LETTER_SORT =
            Sort.by("deadLetterReason").ascending().and(Sort.by("receivedTime").descending());

    @BeforeEach
    public void setUp() {
        deadLetterMessageReportService.setTemplateId("test-template-id");
        deadLetterMessageReportService.setEmailAddresses(List.of("test1@example.com", "test2@example.com"));
    }

    DeadLetterMessageEntity createDeadLetterMessage() {
        ProsecutionConcluded prosecutionConcluded = new ProsecutionConcluded();
        prosecutionConcluded.setMaatId(123);
        DeadLetterMessageEntity deadLetterMessageEntity = new DeadLetterMessageEntity();
        deadLetterMessageEntity.setId(1);
        deadLetterMessageEntity.setDeadLetterReason("Crown Court - Case type not valid for Trial.");
        deadLetterMessageEntity.setReceivedTime(LocalDateTime.of(2025, 01, 01, 10, 20, 0));
        deadLetterMessageEntity.setMessage(prosecutionConcluded);

        return deadLetterMessageEntity;
    }

    @Test
    void testGenerateReport_NoDeadLetterMessagesFound() throws IOException, NotificationClientException {

        when(deadLetterMessageRepository.findByReportingStatus(PENDING, DEAD_LETTER_SORT))
                .thenReturn(Collections.emptyList());

        deadLetterMessageReportService.generateReport();

        verify(deadLetterMessageRepository, times(1)).findByReportingStatus(PENDING, DEAD_LETTER_SORT);
        verify(emailNotificationService, never()).send(anyString(), anyList(), any(File.class), anyString());
    }

    @Test
    void testGenerateReport_DeadLetterMessagesFound() throws IOException, NotificationClientException {

        DeadLetterMessageEntity deadLetterMessageEntity = createDeadLetterMessage();

        List<DeadLetterMessageEntity> deadLetterMessageList = List.of(deadLetterMessageEntity);
        List<Integer> deadLetterIds = deadLetterMessageList.stream()
                .map(DeadLetterMessageEntity::getId)
                .toList();
        File mockFile = Files.createTempFile("temp", ".csv").toFile();
        when(deadLetterMessageRepository.findByReportingStatus(PENDING, DEAD_LETTER_SORT))
                .thenReturn(deadLetterMessageList);

        deadLetterMessageReportService.generateReport();

        verify(deadLetterMessageRepository, times(1)).findByReportingStatus(PENDING, DEAD_LETTER_SORT);
        verify(emailNotificationService, times(1)).send(anyString(), anyList(), any(File.class), anyString());
        verify(deadLetterMessageRepository, times(1)).updateReportingStatusForIds(deadLetterIds, PROCESSED);

        Files.deleteIfExists(mockFile.toPath());
    }

    @Test
    void testGenerateReport_ExceptionDuringFileGeneration() throws IOException, NotificationClientException {

        DeadLetterMessageEntity deadLetterMessageEntity = createDeadLetterMessage();

        List<DeadLetterMessageEntity> deadLetterMessageList = List.of(deadLetterMessageEntity);
        List<Integer> deadLetterIds = deadLetterMessageList.stream()
                .map(DeadLetterMessageEntity::getId)
                .toList();
        when(deadLetterMessageRepository.findByReportingStatus(PENDING, DEAD_LETTER_SORT))
                .thenReturn(deadLetterMessageList);

        try (MockedStatic<GenerateCsvUtil> mockedGenerateCsvUtil = Mockito.mockStatic(GenerateCsvUtil.class)) {
            mockedGenerateCsvUtil
                    .when(() -> GenerateCsvUtil.generateCsvFile(anyList(), anyString()))
                    .thenThrow(new IOException("File generation failed"));
            assertThrows(IOException.class, () -> deadLetterMessageReportService.generateReport());
        }

        verify(deadLetterMessageRepository, times(1)).findByReportingStatus(PENDING, DEAD_LETTER_SORT);
        verify(emailNotificationService, never()).send(anyString(), anyList(), any(File.class), anyString());
        verify(deadLetterMessageRepository, never()).updateReportingStatusForIds(deadLetterIds, PROCESSED);
    }

    @Test
    void testGenerateReport_ExceptionDuringEmailSending() throws IOException, NotificationClientException {

        DeadLetterMessageEntity deadLetterMessageEntity = createDeadLetterMessage();

        List<DeadLetterMessageEntity> deadLetterMessageList = List.of(deadLetterMessageEntity);
        List<Integer> deadLetterIds = deadLetterMessageList.stream()
                .map(DeadLetterMessageEntity::getId)
                .toList();
        when(deadLetterMessageRepository.findByReportingStatus(PENDING, DEAD_LETTER_SORT))
                .thenReturn(deadLetterMessageList);
        File mockFile = Files.createTempFile("temp", ".csv").toFile();
        doThrow(new NotificationClientException("Email sending failed"))
                .when(emailNotificationService)
                .send(anyString(), anyList(), any(File.class), anyString());

        assertThrows(NotificationClientException.class, () -> deadLetterMessageReportService.generateReport());

        verify(deadLetterMessageRepository, times(1)).findByReportingStatus(PENDING, DEAD_LETTER_SORT);
        verify(emailNotificationService, times(1)).send(anyString(), anyList(), any(File.class), anyString());
        verify(deadLetterMessageRepository, never()).updateReportingStatusForIds(deadLetterIds, PROCESSED);

        Files.deleteIfExists(mockFile.toPath());
    }

    @Test
    void testGenerateReportContents_LinesMatchAsExpected() {
        ProsecutionConcluded prosecutionConcluded1 = new ProsecutionConcluded();
        prosecutionConcluded1.setMaatId(123);
        DeadLetterMessageEntity deadLetterMessageEntity1 = new DeadLetterMessageEntity();
        deadLetterMessageEntity1.setDeadLetterReason("Crown Court - Case type not valid for Trial.");
        deadLetterMessageEntity1.setReceivedTime(LocalDateTime.of(2025, 01, 14, 10, 20, 0));
        deadLetterMessageEntity1.setMessage(prosecutionConcluded1);

        ProsecutionConcluded prosecutionConcluded2 = new ProsecutionConcluded();
        prosecutionConcluded2.setMaatId(456);
        DeadLetterMessageEntity deadLetterMessageEntity2 = new DeadLetterMessageEntity();
        deadLetterMessageEntity2.setDeadLetterReason("Crown Court - Case type not valid for Trial.");
        deadLetterMessageEntity2.setReceivedTime(LocalDateTime.of(2025, 01, 12, 11, 20, 20));
        deadLetterMessageEntity2.setMessage(prosecutionConcluded2);

        ProsecutionConcluded prosecutionConcluded3 = new ProsecutionConcluded();
        prosecutionConcluded3.setMaatId(789);
        DeadLetterMessageEntity deadLetterMessageEntity3 = new DeadLetterMessageEntity();
        deadLetterMessageEntity3.setDeadLetterReason("Cannot have Crown Court outcome without Mags Court outcome.");
        deadLetterMessageEntity3.setReceivedTime(LocalDateTime.of(2025, 01, 01, 12, 35, 37));
        deadLetterMessageEntity3.setMessage(prosecutionConcluded3);

        List<DeadLetterMessageEntity> deadLetterMessageList =
                List.of(deadLetterMessageEntity1, deadLetterMessageEntity2, deadLetterMessageEntity3);

        when(deadLetterMessageRepository.findByReportingStatus(PENDING, DEAD_LETTER_SORT))
                .thenReturn(deadLetterMessageList);

        List<String> actual = deadLetterMessageReportService.generateReportContents();

        List<String> expected = List.of(
                new String("MAAT ID, Reason, Received time"),
                new String("123,Crown Court - Case type not valid for Trial.,2025-01-14 10:20:00"),
                new String("456,Crown Court - Case type not valid for Trial.,2025-01-12 11:20:20"),
                new String("789,Cannot have Crown Court outcome without Mags Court outcome.,2025-01-01 12:35:37"),
                new String(System.lineSeparator()),
                new String("Reason for failure, Count, Percentage"),
                new String("Crown Court - Case type not valid for Trial.,2,67%"),
                new String("Cannot have Crown Court outcome without Mags Court outcome.,1,33%"),
                new String("(Of Total),3,"),
                new String(System.lineSeparator()),
                new String("01/01/2025 to 14/01/2025"));

        assertEquals(expected, actual);
    }
}
