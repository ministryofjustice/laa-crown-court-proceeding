package uk.gov.justice.laa.crime.crowncourt.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSentenceOrder {
    private Integer repId;
    private String dbUser;
    private LocalDate sentenceOrderDate;
    private LocalDate dateChanged;
}
