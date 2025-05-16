package uk.gov.justice.laa.crime.crowncourt.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.ProsecutionConcludedEntity;

@Repository
public interface ProsecutionConcludedRepository
        extends JpaRepository<ProsecutionConcludedEntity, Integer> {

    @Query(
            value =
                    "SELECT * FROM crown_court_proceeding.PROSECUTION_CONCLUDED  where STATUS = 'PENDING' AND RETRY_COUNT <= 10",
            nativeQuery = true)
    List<ProsecutionConcludedEntity> getConcludedCases();

    List<ProsecutionConcludedEntity> getByMaatId(Integer maatId);

    List<ProsecutionConcludedEntity> getByHearingId(String hearingId);

    long countByMaatIdAndStatus(Integer maatId, String status);
}
