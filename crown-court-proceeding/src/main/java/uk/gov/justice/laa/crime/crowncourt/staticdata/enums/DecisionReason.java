package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public enum DecisionReason {

    ABANDONED("ABANDONED", "Abandoned"),
    GRANTED("GRANTED", "Granted"),
    FAILMEANS("FAILMEANS", "Failed the Means Test"),
    FAILIOJ("FAILIOJ", "Failed the IoJ Test"),
    FAILMEIOJ("FAILMEIOJ", "Failed Means and IoJ Tests");



    @NotNull
    @JsonPropertyDescription("This will have the decision reason")
    private String code;
    private String description;

    public static DecisionReason getFrom(String code) {
        if (StringUtils.isBlank(code)) return null;

        return Stream.of(DecisionReason.values())
                .filter(ds -> ds.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Decision reason with value: %s does not exist.", code)));
    }

}
