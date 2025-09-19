package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProsecutionCaseIdentifier {

    private String case_urn;
}
