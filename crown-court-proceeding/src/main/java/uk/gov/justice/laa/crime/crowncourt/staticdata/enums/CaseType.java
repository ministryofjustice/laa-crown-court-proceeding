package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.stream.Stream;

/**
 * static data migrated from TOGDATA.CASE_TYPES table
 */
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public enum CaseType {
    INDICTABLE("INDICTABLE", "Indictable", Boolean.TRUE),
    SUMMARY_ONLY("SUMMARY ONLY", "Summary-only", Boolean.TRUE),
    CC_ALREADY("CC ALREADY", "Trial already in Crown Court", Boolean.TRUE),
    APPEAL_CC("APPEAL CC", "Appeal to Crown Court", Boolean.FALSE),
    COMMITAL("COMMITAL", "Committal for Sentence", Boolean.TRUE),
    EITHER_WAY("EITHER WAY", "Either-Way", Boolean.FALSE);

    @NotNull
    @JsonValue
    @JsonPropertyDescription("Specifies the case type")
    private final String caseTypeString;
    private final String description;
    private final Boolean mcooOutcomeRequired;

    public static CaseType getFrom(String caseType) {
        if (StringUtils.isBlank(caseType)) return null;

        return Stream.of(CaseType.values())
                .filter(f -> f.caseTypeString.equals(caseType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("CaseType with value: %s does not exist.", caseType)));
    }
}