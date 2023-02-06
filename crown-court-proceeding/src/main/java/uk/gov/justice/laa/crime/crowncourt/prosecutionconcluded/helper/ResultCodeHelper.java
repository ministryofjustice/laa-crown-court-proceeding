package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.service.MaatCourtDataService;

import java.util.List;


import static uk.gov.justice.laa.crime.crowncourt.constants.CourtDataConstants.NO;
import static uk.gov.justice.laa.crime.crowncourt.constants.CourtDataConstants.YES;
import static uk.gov.justice.laa.crime.crowncourt.enums.CrownCourtTrialOutcome.isConvicted;
import static uk.gov.justice.laa.crime.crowncourt.enums.CrownCourtTrialOutcome.isTrial;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResultCodeHelper {

    private final MaatCourtDataService maatCourtDataService;

    public String isBenchWarrantIssued(final String caseLevelOutcome, List<String> hearingResultCodes) {

        if (isTrial(caseLevelOutcome)) {
            return anyResultCodeMatch(maatCourtDataService.findByCjsResultCodeIn(), hearingResultCodes) ? YES : null;
        }
        return null;
    }

    public String isImprisoned(final String caseLevelOutcome, List<String> hearingResultCodes) {

        if (isConvicted(caseLevelOutcome)) {
            return anyResultCodeMatch(maatCourtDataService.fetchResultCodesForCCImprisonment(), hearingResultCodes) ? YES : NO;
        }
        return null;
    }

    private boolean anyResultCodeMatch(final List<Integer> resultCodes, final List<String> hearingResultCodes) {
        return resultCodes
                .stream()
                .map(String::valueOf)
                .anyMatch(hearingResultCodes::contains);
    }
}