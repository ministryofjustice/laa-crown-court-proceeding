package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class ProsecutionCaseIdentifier {

    private String case_urn;
}
