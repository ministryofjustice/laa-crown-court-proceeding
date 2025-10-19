package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProsecutionCase {

    private UUID id;
    private List<Defendant> defendants;
}
