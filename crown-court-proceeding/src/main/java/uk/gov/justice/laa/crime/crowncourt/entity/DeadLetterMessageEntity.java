package uk.gov.justice.laa.crime.crowncourt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.ProsecutionConcluded;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "DEAD_LETTER_MESSAGE", schema = "crown_court_proceeding")
public class DeadLetterMessageEntity {
    @Id
    @SequenceGenerator(name = "dead_letter_seq", schema = "crown_court_proceeding", sequenceName = "DEAD_LETTER", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dead_letter_seq")
    @Column(name = "ID")
    private Integer id;
    @Column(name = "MESSAGE", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private ProsecutionConcluded message;
    @Column(name = "REASON")
    private String deadLetterReason;
    @Column(name = "RECEIVED_TIME")
    private LocalDateTime receivedTime;
    @Column(name = "REPORTING_STATUS")
    private String reportingStatus;
}
