package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MagCourtOutcome {

    COMMITTED_FOR_TRIAL("COMMITTED FOR TRIAL", "Committed for Trial"),
    SENT_FOR_TRIAL("SENT FOR TRIAL", "Sent for Trial"),
    RESOLVED_IN_MAGS("RESOLVED IN MAGS", "Resolved in Magistrate Court"),
    COMMITTED("COMMITTED", "Committed for Sentence"),
    APPEAL_TO_CC("APPEAL TO CC", "Appeal to Crown Court");

    private String outcome;
    private String description;
}
