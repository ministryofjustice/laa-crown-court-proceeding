package uk.gov.justice.laa.crime.crowncourt.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.justice.laa.crime.crowncourt.exception.ValidationException;

import java.util.Optional;
import java.util.stream.Stream;


@AllArgsConstructor
@Getter
public enum CrownCourtTrialOutcome {

    CONVICTED("CONVICTED"),
    PART_CONVICTED("PART CONVICTED"),
    AQUITTED("AQUITTED");

    private final String value;

    public static boolean isConvicted(String outcome) {

        return Stream.of(CONVICTED, PART_CONVICTED)
                .anyMatch(trOut -> trOut.getValue().equalsIgnoreCase(notEmpty(outcome)));
    }


    public static boolean isTrial(String outcome) {

        return Stream.of(CrownCourtTrialOutcome.values())
                .anyMatch(trOut -> trOut.getValue().equalsIgnoreCase(notEmpty(outcome)));
    }

    private static String notEmpty(String outcome) {

        return Optional.ofNullable(outcome).orElseThrow(
                () -> new ValidationException("Crown Court trial outcome can't be empty."));
    }

}
