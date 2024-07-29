package uk.gov.justice.laa.crime.crowncourt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCase;

import java.util.List;
import java.util.Optional;

public interface ReactivatedProsecutionCaseRepository extends JpaRepository<ReactivatedProsecutionCase, Integer> {
    boolean existsByMaatIdAndReportingStatus(Integer maatId, String reportingStatus);
    Optional<ReactivatedProsecutionCase> findByMaatIdAndReportingStatus(Integer maatId, String reportingStatus);
    List<ReactivatedProsecutionCase> findByReportingStatus(String reportingStatus);
}
