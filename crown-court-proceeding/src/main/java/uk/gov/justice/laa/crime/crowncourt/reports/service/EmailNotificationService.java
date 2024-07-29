package uk.gov.justice.laa.crime.crowncourt.reports.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final NotificationClient client;

    @Value("${emailClient.notify.template-id}")
    private String templateId;

    @Value("#{'${emailClient.notify.recipient}'.split(',')}")
    private List<String> emailAddresses;

    public void send(File reportFile, String fileName) throws NotificationClientException, IOException {
        log.info("Sending email with CSV file");
        byte[] fileContents = FileUtils.readFileToByteArray(reportFile);

        HashMap<String, Object> personalisation = new HashMap<>();
        personalisation.put("date", LocalDate.now());
        personalisation.put("link_to_file", NotificationClient.prepareUpload(fileContents, fileName+".csv"));

        sendEmailToMultipleRecipients(emailAddresses, personalisation);
        log.info("Email sent successfully");
    }

    private void sendEmailToMultipleRecipients(List<String> emailAddresses, HashMap<String, Object> personalisation) {
        emailAddresses.forEach(emailAddress -> {
            try {
                client.sendEmail(templateId,
                        emailAddress,
                        personalisation,
                        null);
            } catch (NotificationClientException clientException) {
                log.error("Failed sending email to recipient with error: {}", clientException.getMessage());
            }
        });
    }
}
