package uk.gov.justice.laa.crime.crowncourt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.ResultEntity;
import uk.gov.justice.laa.crime.crowncourt.model.id.AsnSeqTxnCaseId;

import java.util.List;

@Repository
public interface ResultRepository extends JpaRepository<ResultEntity, AsnSeqTxnCaseId> {


    @Query(value = "SELECT RS.RESULT_CODE FROM MLA.XXMLA_RESULT RS WHERE RS.case_id =:caseId AND RS.ASN_SEQ = :asnSeq",
            nativeQuery = true)
    List<Integer> findResultCodeByCaseIdAndAsnSeq(Integer caseId, String asnSeq);
}
