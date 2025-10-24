package uk.gov.justice.laa.crime.crowncourt.staticdata.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "crown_courts", schema = "crown_court_proceeding")
public class CrownCourtsEntity {
    @Id
    @Column(name = "CODE", nullable = false, length = 6)
    private String id;

    @Column(name = "DESCRIPTION", nullable = false, length = 100)
    private String description;

    @Column(name = "DATE_CREATED", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "USER_CREATED", nullable = false, length = 100)
    private String userCreated;

    @Column(name = "GO_LIVE_DATE")
    private LocalDateTime goLiveDate;

    @Column(name = "CJS_AREA_CODE", length = 2)
    private String cjsAreaCode;

    @Column(name = "OU_CODE", length = 7)
    private String ouCode;
}
