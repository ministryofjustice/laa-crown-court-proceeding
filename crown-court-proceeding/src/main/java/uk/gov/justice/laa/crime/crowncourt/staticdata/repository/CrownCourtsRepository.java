package uk.gov.justice.laa.crime.crowncourt.staticdata.repository;

import uk.gov.justice.laa.crime.crowncourt.staticdata.entity.CrownCourtsEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrownCourtsRepository extends JpaRepository<CrownCourtsEntity, String> {

    Optional<CrownCourtsEntity> findByOuCode(String ouCode);

    boolean existsByOuCode(String ouCode);
}
