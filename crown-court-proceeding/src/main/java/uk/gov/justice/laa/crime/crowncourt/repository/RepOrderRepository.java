package uk.gov.justice.laa.crime.crowncourt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.RepOrderEntity;


@Repository
public interface RepOrderRepository extends JpaRepository<RepOrderEntity, Integer>, JpaSpecificationExecutor<RepOrderEntity> {

}
