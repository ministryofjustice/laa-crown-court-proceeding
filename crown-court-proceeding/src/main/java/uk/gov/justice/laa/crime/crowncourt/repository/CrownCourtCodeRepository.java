package uk.gov.justice.laa.crime.crowncourt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.CrownCourtCode;

import java.util.Optional;

@Repository
public interface CrownCourtCodeRepository extends JpaRepository<CrownCourtCode, String> {

    Optional<CrownCourtCode> findByOuCode(String ouCode);


}
