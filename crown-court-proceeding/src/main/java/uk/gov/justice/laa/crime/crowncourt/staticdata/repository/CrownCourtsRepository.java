package uk.gov.justice.laa.crime.crowncourt.staticdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.staticdata.entity.CrownCourtsEntity;

@Repository
public interface CrownCourtsRepository extends CrudRepository<CrownCourtsEntity, String> {

}