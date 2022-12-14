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
public enum CurrentStatus {
    IN_PROGRESS("IN PROGRESS", "Incomplete"),
    COMPLETE("COMPLETE", "Complete");

    @NotNull
    @JsonPropertyDescription("This will have the current status")
    private String status;
    private String description;

    public static CurrentStatus getFrom(String status) {
        if (StringUtils.isBlank(status)) return null;

        return Stream.of(CurrentStatus.values())
                .filter(f -> f.status.equals(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Status with value: %s does not exist.", status)));
    }
}
