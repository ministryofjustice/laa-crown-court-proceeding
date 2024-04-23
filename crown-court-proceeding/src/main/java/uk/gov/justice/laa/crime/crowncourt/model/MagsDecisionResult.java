package uk.gov.justice.laa.crime.crowncourt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.crime.enums.DecisionReason;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagsDecisionResult {
    private LocalDate decisionDate;
    private DecisionReason decisionReason;
    private LocalDateTime timestamp;
}
