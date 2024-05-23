package uk.gov.justice.laa.crime.crowncourt.repository;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.ReactivatedProsecutionCaseEntity;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.ReportingStatus;

@Repository
public interface ReactivatedProsecutionCaseRepository extends JpaRepository<ReactivatedProsecutionCaseEntity, Integer> {

    List<ReactivatedProsecutionCaseEntity> getByReportingStatus(ReportingStatus reportingStatus);
}
