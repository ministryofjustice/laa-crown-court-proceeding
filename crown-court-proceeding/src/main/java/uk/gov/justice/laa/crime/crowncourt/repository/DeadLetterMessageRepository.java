package uk.gov.justice.laa.crime.crowncourt.repository;

import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DeadLetterMessageRepository extends JpaRepository<DeadLetterMessageEntity, Integer> {
    List<DeadLetterMessageEntity> findByReportingStatus(String reportingStatus, Sort sort);

    @Modifying
    @Transactional
    @Query("UPDATE DeadLetterMessageEntity DLM SET DLM.reportingStatus = :newStatus WHERE DLM.id IN (:ids)")
    void updateReportingStatusForIds(List<Integer> ids, String newStatus);

    @Query(
            value =
                    """
                SELECT *
                FROM crown_court_proceeding.DEAD_LETTER_MESSAGE dlm
                WHERE (dlm.MESSAGE->>'maatId') ~ '^[0-9]+$'
                  AND CAST(dlm.MESSAGE->>'maatId' AS INTEGER) = :maatId
                  AND dlm.REASON IS DISTINCT FROM :excludedReason
                """,
            nativeQuery = true)
    List<DeadLetterMessageEntity> findByMaatIdAndDeadLetterReasonNot(
            @Param("maatId") Integer maatId, @Param("excludedReason") String excludedReason);
}
