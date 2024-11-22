package uk.gov.justice.laa.crime.crowncourt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.DeadLetterMessageEntity;

@Repository
public interface DeadLetterMessageRepository extends JpaRepository<DeadLetterMessageEntity, Integer> {
}
