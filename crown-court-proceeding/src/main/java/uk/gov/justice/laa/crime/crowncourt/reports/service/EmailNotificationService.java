package uk.gov.justice.laa.crime.crowncourt.reports.service;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

  @Override
  public void send(String message) {
    // TODO send notification email to LAA business.
  }
}
