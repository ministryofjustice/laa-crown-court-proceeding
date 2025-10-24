package uk.gov.justice.laa.crime.crowncourt.reports.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private NotificationClient client;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @ParameterizedTest
    @MethodSource("emailAddressesTestData")
    void testSendEmailSuccessfully(int noOfInvokes, List<String> emailAddresses)
            throws IOException, NotificationClientException {

        String fileName = "testFile";
        JSONObject expectedJsonFileObject = getExpectedJsonObject(fileName);
        File mockFile = Files.createTempFile("temp", ".csv").toFile();
        byte[] fileContents = "test data".getBytes(StandardCharsets.UTF_8);

        LocalDate date = LocalDate.now();
        Map<String, Object> mockPersonalisation =
                Map.of("date", date, "link_to_file", NotificationClient.prepareUpload(fileContents, fileName + ".csv"));

        try (MockedStatic<NotificationClient> mockedNotificationClient = Mockito.mockStatic(NotificationClient.class)) {
            mockedNotificationClient
                    .when(() -> NotificationClient.prepareUpload(fileContents, fileName + ".csv"))
                    .thenReturn(expectedJsonFileObject);
        }

        emailNotificationService.send("test-template-id", emailAddresses, mockFile, fileName);

        verify(client, times(noOfInvokes)).sendEmail(anyString(), anyString(), anyMap(), isNull());
        assertEquals(date, mockPersonalisation.get("date"));
        JSONAssert.assertEquals(
                expectedJsonFileObject, (JSONObject) mockPersonalisation.get("link_to_file"), JSONCompareMode.STRICT);
    }

    private JSONObject getExpectedJsonObject(String fileName) {
        JSONObject expectedJsonFileObject = new JSONObject();
        expectedJsonFileObject.put("file", "dGVzdCBkYXRh");
        expectedJsonFileObject.put("filename", fileName + ".csv");
        expectedJsonFileObject.put("confirm_email_before_download", JSONObject.NULL);
        expectedJsonFileObject.put("retention_period", JSONObject.NULL);
        return expectedJsonFileObject;
    }

    private static Stream<Arguments> emailAddressesTestData() {
        return Stream.of(
                Arguments.of(1, List.of("recipient1")),
                Arguments.of(2, List.of("recipient1", "recipient2")),
                Arguments.of(3, List.of("recipient1", "recipient2", "recipient3")));
    }

    @Test
    void testSendEmailIOException() throws NotificationClientException {

        File reportFile = mock(File.class);
        String fileName = "testFile";

        try (MockedStatic<FileUtils> mockedFileUtils = Mockito.mockStatic(FileUtils.class)) {
            mockedFileUtils
                    .when(() -> FileUtils.readFileToByteArray(reportFile))
                    .thenThrow(new IOException("Test IOException"));
            IOException exception = assertThrows(
                    IOException.class,
                    () -> emailNotificationService.send("test-template-id", List.of("test"), reportFile, fileName));

            assertEquals("Test IOException", exception.getMessage());
        }

        verify(client, never()).sendEmail(anyString(), anyString(), anyMap(), isNull());
    }
}
