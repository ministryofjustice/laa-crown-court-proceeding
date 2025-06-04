package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class CalculateAppealOutcomeHelperTest {
  private CalculateAppealOutcomeHelper calculateAppealOutcomeHelper = new CalculateAppealOutcomeHelper();

  @Test
  public void testPartSuccess() {
    assertEquals("PART SUCCESS", calculateAppealOutcomeHelper.calculate("AACD AASA"));
    assertEquals("PART SUCCESS", calculateAppealOutcomeHelper.calculate("AASA AACD"));
  }

  @Test
  public void testSuccessful() {
    assertEquals("SUCCESSFUL", calculateAppealOutcomeHelper.calculate("AACA"));
    assertEquals("SUCCESSFUL", calculateAppealOutcomeHelper.calculate("AASA"));
  }

  @Test
  public void testUnsuccessful() {
    assertEquals("UNSUCCESSFUL", calculateAppealOutcomeHelper.calculate("APA"));
    assertEquals("UNSUCCESSFUL", calculateAppealOutcomeHelper.calculate("AW"));
    assertEquals("UNSUCCESSFUL", calculateAppealOutcomeHelper.calculate("AACD"));
    assertEquals("UNSUCCESSFUL", calculateAppealOutcomeHelper.calculate("ASV"));
    assertEquals("UNSUCCESSFUL", calculateAppealOutcomeHelper.calculate("AASD"));
    assertEquals("UNSUCCESSFUL", calculateAppealOutcomeHelper.calculate("ACSD"));
  }
}

