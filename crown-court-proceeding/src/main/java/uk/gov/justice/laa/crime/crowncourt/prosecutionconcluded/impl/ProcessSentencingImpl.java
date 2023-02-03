package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.impl;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.repository.CrownCourtProcessingRepository;
import uk.gov.justice.laa.crime.crowncourt.util.DateUtil;

import java.time.LocalDate;

import static uk.gov.justice.laa.crime.crowncourt.enums.CrownCourtCaseType.APPEAL_CC;

@Slf4j
@Component
@XRayEnabled
@RequiredArgsConstructor
public class ProcessSentencingImpl {

    private final CrownCourtProcessingRepository crownCourtProcessingRepository;

    @Value("${spring.datasource.username}")
    private String dbUser;

    public void processSentencingDate(String ccCaseEndDate, Integer maatId, String catyType) {

        log.info("Processing sentencing date");
        LocalDate caseEndDate = DateUtil.parse(ccCaseEndDate);
        if (caseEndDate != null) {
            String user = dbUser != null ? dbUser.toUpperCase() : null;
            if (APPEAL_CC.getValue().equalsIgnoreCase(catyType)) {
                crownCourtProcessingRepository
                        .invokeUpdateAppealSentenceOrderDate(maatId,
                                user,
                                caseEndDate,
                                LocalDate.now()
                        );
            } else {
                crownCourtProcessingRepository
                        .invokeUpdateSentenceOrderDate(maatId,
                                user,
                                caseEndDate
                        );
            }
        }
    }
}