package uk.gov.justice.laa.crime.crowncourt.staticdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.entity.CrownCourtsEntity;

import java.util.Optional;

@Repository
public interface CrownCourtsRepository extends JpaRepository<CrownCourtsEntity, String> {

    Optional<CrownCourtsEntity> findByOuCode(String ouCode);
}