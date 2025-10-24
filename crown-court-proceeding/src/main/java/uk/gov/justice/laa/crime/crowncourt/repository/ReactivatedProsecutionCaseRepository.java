package uk.gov.justice.laa.crime.crowncourt.repository;

import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCase;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ReactivatedProsecutionCaseRepository extends JpaRepository<ReactivatedProsecutionCase, Integer> {
    boolean existsByMaatIdAndReportingStatus(Integer maatId, String reportingStatus);

    Optional<ReactivatedProsecutionCase> findByMaatIdAndReportingStatus(Integer maatId, String reportingStatus);

    List<ReactivatedProsecutionCase> findByReportingStatus(String reportingStatus);

    @Modifying
    @Transactional
    @Query(
            "UPDATE ReactivatedProsecutionCase RPC SET RPC.reportingStatus = :processed WHERE RPC.reportingStatus =:pending")
    void updateReportingStatus(String processed, String pending);
}
