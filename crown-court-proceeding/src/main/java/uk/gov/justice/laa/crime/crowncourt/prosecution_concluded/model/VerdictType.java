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
public class VerdictType {
    @JsonProperty("verdictTypeId")
    private UUID verdictTypeId;

    @JsonProperty("sequence")
    private Integer sequence;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("categoryType")
    private String categoryType;
}
