package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offence {

    private String id;
    private List<JudicialResult> judicial_results;
}
