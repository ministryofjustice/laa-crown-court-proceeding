package uk.gov.justice.laa.crime.crowncourt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.laa.crime.crowncourt.entity.RepOrderCCOutComeEntity;

import javax.transaction.Transactional;
import java.time.LocalDate;

@Repository
public interface CrownCourtProcessingRepository extends JpaRepository<RepOrderCCOutComeEntity, Integer> {


    @Transactional
    @Procedure(procedureName = "togdata.application.update_appeal_sentence_ord_dt")
    void invokeUpdateAppealSentenceOrderDate(
            @Param("p_rep_id") Integer repId,
            @Param("p_user") String dbUser,
            @Param("p_sentence_order_date") LocalDate sentenceOrderDate,
            @Param("p_date_changed") LocalDate dateChanged);

    @Transactional
    @Procedure(procedureName = "togdata.application.update_cc_sentence_order_dt")
    void invokeUpdateSentenceOrderDate(
            @Param("p_rep_id") Integer repId,
            @Param("p_user") String dbUser,
            @Param("p_sentence_order_date") LocalDate sentenceOrderDate);

}
