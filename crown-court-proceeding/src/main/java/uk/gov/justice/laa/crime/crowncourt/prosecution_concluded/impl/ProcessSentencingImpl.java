package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.impl;

import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtCaseType.APPEAL_CC;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.model.UpdateSentenceOrder;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAPIService;
import uk.gov.justice.laa.crime.util.DateUtil;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessSentencingImpl {

    private final CourtDataAPIService courtDataAPIService;

    @Value("${feature.prosecution-concluded-listener.dbUsername}")
    private String dbUser;

    public void processSentencingDate(String ccCaseEndDate, Integer maatId, String catyType) {

        log.info("Processing sentencing date");
        LocalDate caseEndDate = DateUtil.parse(ccCaseEndDate);
        if (caseEndDate != null) {
            String user = dbUser != null ? dbUser.toUpperCase() : null;
            if (APPEAL_CC.getValue().equalsIgnoreCase(catyType)) {
                courtDataAPIService.invokeUpdateAppealSentenceOrderDate(UpdateSentenceOrder.builder()
                        .repId(maatId)
                        .dbUser(user)
                        .sentenceOrderDate(caseEndDate)
                        .dateChanged(LocalDate.now())
                        .build());
            } else {
                courtDataAPIService.invokeUpdateSentenceOrderDate(UpdateSentenceOrder.builder()
                        .repId(maatId)
                        .dbUser(user)
                        .sentenceOrderDate(caseEndDate)
                        .build());
            }
        }
    }
}
