package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.helper;

import static java.lang.String.format;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.laa.crime.crowncourt.staticdata.entity.CrownCourtsEntity;
import uk.gov.justice.laa.crime.crowncourt.staticdata.repository.CrownCourtsRepository;
import uk.gov.justice.laa.crime.exception.ValidationException;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrownCourtCodeHelper {

    private final CrownCourtsRepository crownCourtCodeRepository;

    public String getCode(String ouCode) {
        log.info("Getting Crown Court Code");

        Optional<CrownCourtsEntity> optCrownCourtCode = crownCourtCodeRepository.findByOuCode(ouCode);
        CrownCourtsEntity crownCourtCode = optCrownCourtCode.orElseThrow(
                () -> new ValidationException(format("Crown Court Code Look Up Failed for %s", ouCode)));

        return crownCourtCode.getId();
    }

    public boolean isValidCode(String ouCode) {
        return crownCourtCodeRepository.existsByOuCode(ouCode);
    }
}
