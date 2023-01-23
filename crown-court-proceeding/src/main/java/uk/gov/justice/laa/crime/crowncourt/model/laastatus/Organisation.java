package uk.gov.justice.laa.crime.crowncourt.model.laastatus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("address")
    @Expose
    public Address address;
    @SerializedName("contact")
    @Expose
    public Contact contact;

}
