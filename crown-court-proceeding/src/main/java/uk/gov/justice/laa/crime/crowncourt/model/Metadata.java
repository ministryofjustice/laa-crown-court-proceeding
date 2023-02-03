package uk.gov.justice.laa.crime.crowncourt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Metadata {

    private String laaTransactionId;
}
