package uk.gov.justice.laa.crime.crowncourt.model.laastatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Offence {

    @JsonProperty("offence_id")
    private String offenceId;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("status_date")
    private String statusDate;

    @JsonProperty("effective_start_date")
    private String effectiveStartDate;

    @JsonProperty("effective_end_date")
    private String effectiveEndDate;
}
