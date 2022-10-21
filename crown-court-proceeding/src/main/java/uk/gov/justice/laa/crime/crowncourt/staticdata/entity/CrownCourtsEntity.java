package uk.gov.justice.laa.crime.crowncourt.staticdata.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
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