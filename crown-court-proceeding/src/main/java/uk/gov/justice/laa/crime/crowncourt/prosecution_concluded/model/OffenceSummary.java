package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenceSummary {
    @JsonProperty("offenceId")
    private UUID offenceId;
    @JsonProperty("offenceCode")
    private String offenceCode;
    @JsonProperty("proceedingsConcluded")
    private boolean proceedingsConcluded;
    @JsonProperty("plea")
    private Plea plea;
    @JsonProperty("verdict")
    private Verdict verdict;
    @JsonProperty("proceedingsConcludedChangedDate")
    private String proceedingsConcludedChangedDate;
    @JsonProperty("judicialResults")
    private List<Result> judicialResults;
}
