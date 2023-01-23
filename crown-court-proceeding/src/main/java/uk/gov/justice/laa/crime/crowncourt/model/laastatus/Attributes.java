package uk.gov.justice.laa.crime.crowncourt.model.laastatus;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attributes {

    @SerializedName("maat_reference")
    @Expose
    private Integer maatReference;
    @SerializedName("defence_organisation")
    @Expose
    private DefenceOrganisation defenceOrganisation;
    @SerializedName("offences")
    @Expose
    private List<Offence> offences;

}
