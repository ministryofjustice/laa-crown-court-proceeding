package uk.gov.justice.laa.crime.crowncourt.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;

import java.util.List;

@Repository
public interface ProsecutionConcludedRepository extends JpaRepository<ProsecutionConcludedEntity, Integer> {

    @Query(value = "SELECT * FROM MLA.XXMLA_PROSECUTION_CONCLUDED  where STATUS = 'PENDING' AND RETRY_COUNT <= 10", nativeQuery = true)
    List<ProsecutionConcludedEntity> getConcludedCases();

    List<ProsecutionConcludedEntity> getByMaatId(Integer maatId);

    List<ProsecutionConcludedEntity> getByHearingId(String hearingId);
}
