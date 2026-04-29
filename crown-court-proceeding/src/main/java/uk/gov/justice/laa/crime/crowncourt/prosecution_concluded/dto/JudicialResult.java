package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudicialResult {

    private Boolean is_convicted_result;
}
