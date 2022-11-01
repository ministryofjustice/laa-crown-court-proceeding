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
public enum ReviewResult {

    PASS("PASS"),
    FAIL("FAIL");

    @NotNull
    @JsonPropertyDescription("Determines review result")
    private String reviewResult;


    public static ReviewResult getFrom(String result) {
        if (StringUtils.isBlank(result)) return null;

        return Stream.of(ReviewResult.values())
                .filter(rr -> rr.reviewResult.equals(result))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Review result with value: %s does not exist.", result)));
    }
}
