package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;



import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Hearing {

    private UUID id;
    private List<ProsecutionCase> prosecution_cases;

}