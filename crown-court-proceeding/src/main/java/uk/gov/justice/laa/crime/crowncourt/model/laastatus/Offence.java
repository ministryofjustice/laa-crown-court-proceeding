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
public class Offence {

    @SerializedName("offence_id")
    @Expose
    public String offenceId;
    @SerializedName("status_code")
    @Expose
    public String statusCode;
    @SerializedName("status_date")
    @Expose
    public String statusDate;
    @SerializedName("effective_start_date")
    @Expose
    public String effectiveStartDate;
    @SerializedName("effective_end_date")
    @Expose
    public String effectiveEndDate;
}