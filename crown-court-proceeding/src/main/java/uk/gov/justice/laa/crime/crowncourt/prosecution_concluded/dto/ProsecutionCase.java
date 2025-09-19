package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProsecutionCase {

    private String id;
    private ProsecutionCaseIdentifier prosecution_case_identifier;
    private List<Defendant> defendants;
}
