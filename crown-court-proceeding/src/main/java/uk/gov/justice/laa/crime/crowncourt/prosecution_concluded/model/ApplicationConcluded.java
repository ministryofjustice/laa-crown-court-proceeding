package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationConcluded {
    @JsonProperty("applicationId")
    private UUID applicationId;

    @JsonProperty("subjectId")
    private UUID subjectId;

    @JsonProperty("applicationResultCode")
    private String applicationResultCode;
}
