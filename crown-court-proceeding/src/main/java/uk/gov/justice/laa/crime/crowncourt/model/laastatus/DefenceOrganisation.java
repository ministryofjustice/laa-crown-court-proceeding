package uk.gov.justice.laa.crime.crowncourt.model.laastatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefenceOrganisation {

    @JsonProperty("laa_contract_number")
    private String laaContractNumber;

    @JsonProperty("sra_number")
    private String sraNumber;

    @JsonProperty("bar_council_membership_number")
    private String barCouncilMembershipNumber;

    @JsonProperty("incorporation_number")
    private String incorporationNumber;

    @JsonProperty("registered_charity_number")
    private String registeredCharityNumber;

    @JsonProperty("organisation")
    private Organisation organisation;
}
