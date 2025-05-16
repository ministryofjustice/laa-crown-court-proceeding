package uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FinAssIncomeEvidenceDTO implements Serializable {
    private Integer id;
    private LocalDateTime dateReceived;
    private LocalDateTime dateCreated;
    private String userCreated;
    private LocalDateTime dateModified;
    private String userModified;
    private String active;
    private LocalDateTime removedDate;
    private String mandatory;
    private String otherText;
    private String adhoc;
    private String incomeEvidence;
    private Integer applicant;
}
