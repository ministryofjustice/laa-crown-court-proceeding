package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OffenceSummary {
    private UUID offenceId;
    private String offenceCode;
    private boolean proceedingsConcluded;
    private Plea plea;
    private Verdict verdict;
    private String proceedingsConcludedChangedDate;
}
