package uk.gov.justice.laa.crime.crowncourt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.QueueMessageLogEntity;

import java.time.LocalDateTime;

@Repository
public interface QueueMessageLogRepository extends JpaRepository<QueueMessageLogEntity, Integer> {

   long deleteByCreatedTimeBefore(LocalDateTime purgeBeforeDate);
}
