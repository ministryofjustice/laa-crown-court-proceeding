package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum MagCourtOutcome {

    COMMITTED_FOR_TRIAL("COMMITTED FOR TRIAL", "Committed for Trial"),
    SENT_FOR_TRIAL("SENT FOR TRIAL", "Sent for Trial"),
    RESOLVED_IN_MAGS("RESOLVED IN MAGS", "Resolved in Magistrate Court"),
    COMMITTED("COMMITTED", "Committed for Sentence"),
    APPEAL_TO_CC("APPEAL TO CC", "Appeal to Crown Court");

    @JsonPropertyDescription("This will have magistrate court outcome")
    private String outcome;
    private String description;

    public static MagCourtOutcome getFrom(String outcome) {
        if (StringUtils.isBlank(outcome)) return null;

        return Stream.of(MagCourtOutcome.values())
                .filter(mo -> mo.outcome.equals(outcome))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("outcome with value: %s does not exist.", outcome)));
    }
}
