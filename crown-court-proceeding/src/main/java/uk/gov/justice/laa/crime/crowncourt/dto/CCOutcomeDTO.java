package uk.gov.justice.laa.crime.crowncourt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CCOutcomeDTO {

    private String outCome;
    private String description;
    private LocalDateTime outcomeDate;
}
