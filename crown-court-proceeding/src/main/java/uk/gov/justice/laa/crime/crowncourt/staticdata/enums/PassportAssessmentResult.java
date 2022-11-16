package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PassportAssessmentResult {
    PASS("PASS", "Pass"),
    FAIL("FAIL", "Fail"),
    TEMP("TEMP", "Temporary Pass"),
    FAIL_CONTINUE("FAIL CONTINUE", "Fail-Benefits Bypass");

    private String result;
    private String reason;

    public static PassportAssessmentResult getFrom(String result) {
        if (StringUtils.isBlank(result)) return null;

        return Stream.of(PassportAssessmentResult.values())
                .filter(p -> p.result.equals(result))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Passport Assessment Result with value: %s does not exist.", result)));
    }
}
