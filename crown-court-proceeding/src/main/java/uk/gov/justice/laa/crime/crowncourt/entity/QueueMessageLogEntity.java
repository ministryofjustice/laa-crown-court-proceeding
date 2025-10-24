package uk.gov.justice.laa.crime.crowncourt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "QUEUE_MESSAGE_LOG", schema = "crown_court_proceeding")
public class QueueMessageLogEntity {
    @Id
    @Column(name = "TRANSACTION_UUID")
    private String transactionUUID;

    @Column(name = "LAA_TRANSACTION_ID")
    private String laaTransactionId;

    @Column(name = "MAAT_ID")
    private Integer maatId;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "MESSAGE")
    private byte[] message;

    @Column(name = "CREATED_TIME")
    private LocalDateTime createdTime;
}
