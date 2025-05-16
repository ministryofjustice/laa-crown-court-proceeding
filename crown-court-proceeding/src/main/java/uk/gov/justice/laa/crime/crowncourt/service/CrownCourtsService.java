package uk.gov.justice.laa.crime.crowncourt.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.crime.crowncourt.staticdata.entity.CrownCourtsEntity;
import uk.gov.justice.laa.crime.crowncourt.staticdata.repository.CrownCourtsRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrownCourtsService {

    private final CrownCourtsRepository incomeEvidenceRepository;

    public Optional<CrownCourtsEntity> getById(String id) {
        return incomeEvidenceRepository.findById(id);
    }
}
