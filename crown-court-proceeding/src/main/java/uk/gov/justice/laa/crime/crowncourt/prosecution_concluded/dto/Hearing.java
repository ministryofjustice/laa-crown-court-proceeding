package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hearing {

    private UUID id;
    private String jurisdiction_type;
    private List<ProsecutionCase> prosecution_cases;
    private CourtCentre court_centre;
}
