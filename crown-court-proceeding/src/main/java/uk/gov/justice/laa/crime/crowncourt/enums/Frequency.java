package uk.gov.justice.laa.crime.crowncourt.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.justice.laa.crime.crowncourt.prosecutionconcluded.helper.PersistableEnum;

@Getter
@AllArgsConstructor
public enum Frequency implements PersistableEnum<String> {

    WEEKLY("WEEKLY", "Weekly", 52),
    TWO_WEEKLY("2WEEKLY", "2 Weekly", 26),
    FOUR_WEEKLY("4WEEKLY", "4 Weekly", 13),
    MONTHLY("MONTHLY", "Monthly", 12),
    ANNUALLY("ANNUALLY", "Annually", 1);

    @JsonValue
    private final String code;
    private final String description;
    private final int annualWeighting;

    @Override
    public String getValue() {
        return this.code;
    }

}
