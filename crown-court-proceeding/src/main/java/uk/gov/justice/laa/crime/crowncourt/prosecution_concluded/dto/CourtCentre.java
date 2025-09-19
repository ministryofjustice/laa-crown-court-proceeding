package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtCentre {

    private String id;
    private String oucode_l2_code;
    private String code;
}
