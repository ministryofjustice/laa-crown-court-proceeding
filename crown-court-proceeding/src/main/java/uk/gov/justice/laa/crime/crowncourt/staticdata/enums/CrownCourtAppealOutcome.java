package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;

import java.util.Optional;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum CrownCourtAppealOutcome {

    SUCCESSFUL("SUCCESSFUL"),
    UNSUCCESSFUL("UNSUCCESSFUL"),
    PART_SUCCESS("PART SUCCESS");

    private String value;

    public static boolean isAppeal(String outcome) {

        return Stream.of(CrownCourtAppealOutcome.values())
                .anyMatch(appOut -> appOut.getValue().equalsIgnoreCase(notEmpty(outcome)));
    }


    private static String notEmpty(String outcome) {

        return Optional.ofNullable(outcome).orElseThrow(
                () -> new ValidationException("Crown Court appeal outcome can't be empty."));
    }
}
