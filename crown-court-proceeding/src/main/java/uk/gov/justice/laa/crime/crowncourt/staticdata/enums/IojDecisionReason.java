package uk.gov.justice.laa.crime.crowncourt.staticdata.enums;

import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum IojDecisionReason {
    NOTUNDPROC("NOTUNDPROC", 1, "Would not understand the proceedings"),
    SKILLEXAM("SKILLEXAM", 2, "Skillful exam of prosecution witness"),
    LOSLIBTY("LOSLIBTY", 3, "Loss of liberty"),
    SUSPSENT("SUSPSENT", 4, "Subject to a suspended sentence"),
    LOSSLIVEHD("LOSSLIVEHD", 5, "Loss of livelihood"),
    SEDAMTOREP("SEDAMTOREP", 6, "Serious damage to reputation"),
    SUBQUELAW("SUBQUELAW", 7, "Substantial question of law"),
    WITTRACE("WITTRACE", 8, "Witnesses to be traced"),
    INTANOPERS("INTANOPERS", 9, "In the interests of another person"),
    OTHER("OTHER", 10, "Other");

    private String code;
    private int sequence;
    private String description;

    public static IojDecisionReason getFrom(String code) {
        if (StringUtils.isBlank(code)) return null;

        return Stream.of(IojDecisionReason.values())
                .filter(iojDecisionReason -> iojDecisionReason.code.equals(code))
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "IOJ Decision Outcome with value: %s does not exist.",
                                                code)));
    }
}
