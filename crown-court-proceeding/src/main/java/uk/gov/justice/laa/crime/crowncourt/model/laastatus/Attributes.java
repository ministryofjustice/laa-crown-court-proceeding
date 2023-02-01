package uk.gov.justice.laa.crime.crowncourt.model.laastatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attributes {

    @JsonProperty("maat_reference")
    private Integer maatReference;
    @JsonProperty("defence_organisation")
    private DefenceOrganisation defenceOrganisation;
    @JsonProperty("offences")
    private List<Offence> offences;

}
