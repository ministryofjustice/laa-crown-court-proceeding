package uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WQHearingDTO {

    private Integer txId;
    private Integer caseId;
    private String hearingUUID;
    private Integer maatId;
    private String wqJurisdictionType;
    private String ouCourtLocation;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
    private String caseUrn;
    private String resultCodes;
}
