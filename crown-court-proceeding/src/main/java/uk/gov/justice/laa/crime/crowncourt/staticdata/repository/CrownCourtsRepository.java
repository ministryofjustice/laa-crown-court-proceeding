package uk.gov.justice.laa.crime.crowncourt.staticdata.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.entity.CrownCourtsEntity;

@Repository
public interface CrownCourtsRepository extends JpaRepository<CrownCourtsEntity, String> {

    Optional<CrownCourtsEntity> findByOuCode(String ouCode);

    boolean existsByOuCode(String ouCode);
}
