package uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.crime.crowncourt.model.Metadata;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProsecutionConcluded {

    private Integer maatId;
    private UUID defendantId;
    private UUID prosecutionCaseId;
    private boolean isConcluded;
    private UUID hearingIdWhereChangeOccurred;
    private List<OffenceSummary> offenceSummary;
    private int messageRetryCounter;
    private int retryCounterForHearing;
    private Metadata metadata;
    private boolean isProcessed;

}