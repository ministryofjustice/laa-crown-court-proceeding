package uk.gov.justice.laa.crime.crowncourt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Types;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PROSECUTION_CONCLUDED", schema = "crown_court_proceeding")
public class ProsecutionConcludedEntity {

    @Id
    @SequenceGenerator(name = "case_con_seq", sequenceName = "CASE_CONCLUSION", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_con_seq")
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MAAT_ID")
    private Integer maatId;

    @Column(name = "HEARING_ID")
    private String hearingId;

    @Lob
    @JdbcTypeCode(Types.VARBINARY)
    @Column(name = "CASE_DATA")
    private byte[] caseData;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_TIME")
    private LocalDateTime createdTime;

    @Column(name = "UPDATED_TIME")
    private LocalDateTime updatedTime;

    @Column(name = "RETRY_COUNT")
    private Integer retryCount;
}
