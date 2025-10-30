package uk.gov.justice.laa.crime.crowncourt.repository;

import uk.gov.justice.laa.crime.crowncourt.entity.QueueMessageLogEntity;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueMessageLogRepository extends JpaRepository<QueueMessageLogEntity, Integer> {

    long deleteByCreatedTimeBefore(LocalDateTime purgeBeforeDate);
}
