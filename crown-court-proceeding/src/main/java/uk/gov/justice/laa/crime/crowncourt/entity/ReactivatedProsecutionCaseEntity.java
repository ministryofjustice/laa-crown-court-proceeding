package uk.gov.justice.laa.crime.crowncourt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import uk.gov.justice.laa.crime.crowncourt.staticdata.enums.ReportingStatus;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "REACTIVATED_PROSECUTION_CASE", schema = "crown_court_proceeding")
public class ReactivatedProsecutionCaseEntity {

    @Id
    @SequenceGenerator(name = "reactivated_prosecution_case_seq", sequenceName = "REACTIVATED_PROSECUTION_CASE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reactivated_prosecution_case_seq")
    @Column(name = "ID")
    private Integer id;
    @Column(name = "MAAT_ID")
    private Integer maatId;
    @Column(name = "CASE_URN")
    private String caseUrn;
    @Column(name = "HEARING_ID")
    private String hearingId;
    @Column(name = "PREVIOUS_OUTCOME")
    private String previousOutcome;
    @Column(name = "PREVIOUS_OUTCOME_DATE")
    private LocalDateTime previousOutcomeDate;
    @Column(name = "DATE_OF_STATUS_CHANGE")
    private LocalDateTime dateOfStatusChange;
    @Column(name = "REPORTING_STATUS")
    private ReportingStatus reportingStatus;
}
