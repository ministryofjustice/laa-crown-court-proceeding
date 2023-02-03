package uk.gov.justice.laa.crime.crowncourt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.WQOffenceEntity;

@Repository
public interface WQOffenceRepository extends JpaRepository<WQOffenceEntity, Integer> {

    @Query(value = "SELECT COUNT(*) FROM MLA.XXMLA_WQ_OFFENCE WHERE CASE_ID = ?1 AND OFFENCE_ID = ?2 AND CC_NEW_OFFENCE = 'Y' AND APPLICATION_FLAG = 0", nativeQuery = true)
    Integer getNewOffenceCount(Integer caseId, String offenceId);
}
