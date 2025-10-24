package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CalculateAppealOutcomeHelper {
    private static final String SUCCESSFUL = "SUCCESSFUL";
    private static final String UNSUCCESSFUL = "UNSUCCESSFUL";
    private static final String PART_SUCCESS = "PART SUCCESS";

    public String calculate(String applicationResult) {
        String outcome = UNSUCCESSFUL;
        if (applicationResult.equals("AACA") || applicationResult.equals("AASA")) {
            outcome = SUCCESSFUL;
        } else if (applicationResult.contains("AACD") && applicationResult.contains("AASA")) {
            outcome = PART_SUCCESS;
        }
        return outcome;
    }
}
