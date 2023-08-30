package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.CourtDataAPIService;

import java.util.List;

import static uk.gov.justice.laa.crime.crowncourt.common.Constants.NO;
import static uk.gov.justice.laa.crime.crowncourt.common.Constants.YES;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.isConvicted;
import static uk.gov.justice.laa.crime.crowncourt.staticdata.enums.CrownCourtTrialOutcome.isTrial;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResultCodeHelper {

    private final CourtDataAPIService courtDataAPIService;

    public String isBenchWarrantIssued(final String caseLevelOutcome, List<String> hearingResultCodes) {

        if (isTrial(caseLevelOutcome)) {
            return anyResultCodeMatch(courtDataAPIService.findByCjsResultCodeIn(), hearingResultCodes) ? YES : null;
        }
        return null;
    }

    public String isImprisoned(final String caseLevelOutcome, List<String> hearingResultCodes) {

        if (isConvicted(caseLevelOutcome)) {
            return anyResultCodeMatch(courtDataAPIService.fetchResultCodesForCCImprisonment(), hearingResultCodes) ? YES : NO;
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