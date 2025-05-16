package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProsecutionConcluded {

    @JsonProperty("maatId")
    private Integer maatId;
    @JsonProperty("defendantId")
    private UUID defendantId;
    @JsonProperty("prosecutionCaseId")
    private UUID prosecutionCaseId;
    @JsonProperty("isConcluded")
    private boolean isConcluded;
    @JsonProperty("hearingIdWhereChangeOccurred")
    private UUID hearingIdWhereChangeOccurred;
    @JsonProperty("offenceSummary")
    private List<OffenceSummary> offenceSummary;
    @JsonProperty("messageRetryCounter")
    private int messageRetryCounter;
    @JsonProperty("retryCounterForHearing")
    private int retryCounterForHearing;
    @JsonProperty("metadata")
    private Metadata metadata;
    @JsonProperty("isProcessed")
    private boolean isProcessed;
    @JsonProperty("applicationConcluded")
    private ApplicationConcluded applicationConcluded;
}