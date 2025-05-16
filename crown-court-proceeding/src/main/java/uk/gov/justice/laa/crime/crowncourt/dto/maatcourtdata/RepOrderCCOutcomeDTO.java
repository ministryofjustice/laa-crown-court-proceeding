package uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepOrderCCOutcomeDTO {

    private int id;
    private int repId;
    private String outcome;
    private String description;
    private LocalDateTime outcomeDate;
    private String userCreated;
    private LocalDateTime dateCreated;
    private String caseNumber;
    private String crownCourtCode;
    private String userModified;
    private LocalDateTime dateModified;
}
