package uk.gov.justice.laa.crime.crowncourt.model.laastatus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;


@Builder
@NoArgsConstructor (force = true)
@AllArgsConstructor
@Value
public class LaaStatusUpdate {

    @SerializedName("data")
    @Expose
    RepOrderData data;
}
