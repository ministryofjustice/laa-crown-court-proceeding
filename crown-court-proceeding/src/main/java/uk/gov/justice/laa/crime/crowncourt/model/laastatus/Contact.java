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
public class Contact {

    @JsonProperty("home")
    private String home;

    @JsonProperty("work")
    private String work;

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("primary_email")
    private String primaryEmail;

    @JsonProperty("secondary_email")
    private String secondaryEmail;

    @JsonProperty("fax")
    private String fax;
}
