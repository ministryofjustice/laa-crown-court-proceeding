package uk.gov.justice.laa.crime.crowncourt.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "CROWN_COURTS", schema = "TOGDATA")
public class CrownCourtCode {
    @Id
    @Column(name = "CODE")
    private String code;
    @Column(name = "OU_CODE")
    private String ouCode;

}
