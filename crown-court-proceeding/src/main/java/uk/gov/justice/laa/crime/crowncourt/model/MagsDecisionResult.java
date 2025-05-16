package uk.gov.justice.laa.crime.crowncourt.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.crime.enums.DecisionReason;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MagsDecisionResult {
    @NotNull private LocalDate decisionDate;
    @NotNull private LocalDateTime timestamp;
    @NotNull private DecisionReason decisionReason;
}
