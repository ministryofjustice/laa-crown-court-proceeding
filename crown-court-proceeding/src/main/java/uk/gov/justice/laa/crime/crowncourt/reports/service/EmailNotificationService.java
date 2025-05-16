package uk.gov.justice.laa.crime.crowncourt.reports.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private static final String DATE = "date";
    private static final String LINK_TO_FILE = "link_to_file";

    private final NotificationClient client;

    public void send(
            String templateId, List<String> emailAddresses, File reportFile, String fileName)
            throws NotificationClientException, IOException {
        log.info("Sending email with CSV file");
        byte[] fileContents = FileUtils.readFileToByteArray(reportFile);

        Map<String, Object> personalisation =
                Map.of(
                        DATE, LocalDate.now(),
                        LINK_TO_FILE,
                                NotificationClient.prepareUpload(fileContents, fileName + ".csv"));

        sendEmailToMultipleRecipients(templateId, emailAddresses, personalisation);
    }

    private void sendEmailToMultipleRecipients(
            String templateId,
            @NonNull List<String> emailAddresses,
            Map<String, Object> personalisation) {
        emailAddresses.forEach(
                emailAddress -> sendEmailToRecipient(templateId, emailAddress, personalisation));
    }

    private void sendEmailToRecipient(
            String templateId, String emailAddress, Map<String, Object> personalisation) {
        try {
            client.sendEmail(templateId, emailAddress, personalisation, null);
            log.info("Email sent successfully");
        } catch (NotificationClientException clientException) {
            log.error(
                    "Failed sending email to recipient '{}' with error: {}",
                    emailAddress,
                    clientException.getMessage());
        }
    }
}
