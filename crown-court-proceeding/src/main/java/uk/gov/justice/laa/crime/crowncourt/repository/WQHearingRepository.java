package uk.gov.justice.laa.crime.crowncourt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.WQHearingEntity;

import java.util.List;

@Repository
public interface WQHearingRepository extends JpaRepository<WQHearingEntity, String> {

    List<WQHearingEntity> findByMaatIdAndHearingUUID(Integer maatId, String hearingUuid);

}