package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Defendant {

    private UUID id;
    private List<Offence> offences;

}
