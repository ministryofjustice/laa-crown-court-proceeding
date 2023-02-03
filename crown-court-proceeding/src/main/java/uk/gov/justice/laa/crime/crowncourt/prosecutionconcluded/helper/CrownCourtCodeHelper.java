package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.crime.crowncourt.entity.CrownCourtCode;
import uk.gov.justice.laa.crime.crowncourt.exception.MAATCourtDataException;
import uk.gov.justice.laa.crime.crowncourt.repository.CrownCourtCodeRepository;

import java.util.Optional;

import static java.lang.String.format;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrownCourtCodeHelper {

    private final CrownCourtCodeRepository crownCourtCodeRepository;

    public String getCode(String ouCode) {
        log.info("Getting Crown Court Code");

        Optional<CrownCourtCode> optCrownCourtCode = crownCourtCodeRepository.findByOuCode(ouCode);
        CrownCourtCode crownCourtCode = optCrownCourtCode.orElseThrow(()
                -> new MAATCourtDataException(format("Crown Court Code Look Up Failed for %s", ouCode)));

        return crownCourtCode.getCode();
    }
}