package uk.gov.justice.laa.crime.crowncourt.model.id;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsnSeqTxnCaseId implements Serializable {

    private Integer txId;
    private Integer caseId;
    private String asnSeq;
}
