package uk.gov.justice.laa.crime.crowncourt.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewWorkReason {

    @JsonCreator
    public NewWorkReason(String code) {
        this.code = code;
    }

    @JsonValue
    private String code;
    private String type;
    private String description;
    private LocalDateTime dateCreated;
    private String userCreated;
    private LocalDateTime dateModified;
    private String userModified;
    private Integer sequence;
    private String enabled;
    private String raGroup;
    private String initialDefault;
}
