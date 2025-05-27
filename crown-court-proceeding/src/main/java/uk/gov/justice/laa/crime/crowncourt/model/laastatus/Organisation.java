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
public class Organisation {

    @JsonProperty("name")
    public String name;

    @JsonProperty("address")
    public Address address;

    @JsonProperty("contact")
    public Contact contact;
}
