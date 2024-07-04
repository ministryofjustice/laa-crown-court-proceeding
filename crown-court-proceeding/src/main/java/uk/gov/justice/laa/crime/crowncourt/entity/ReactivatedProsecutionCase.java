package uk.gov.justice.laa.crime.crowncourt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "REACTIVATED_PROSECUTION_CASE", schema = "crown_court_proceeding")
public class ReactivatedProsecutionCase {
    @Id
    @SequenceGenerator(name = "reactivate_case_seq", schema = "crown_court_proceeding", sequenceName = "REACTIVATE_CASE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reactivate_case_seq")
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
    @CreationTimestamp
    @Column(name = "DATE_OF_STATUS_CHANGE")
    private LocalDateTime dateOfStatusChange;
    @Column(name = "REPORTING_STATUS")
    private String reportingStatus;
}
