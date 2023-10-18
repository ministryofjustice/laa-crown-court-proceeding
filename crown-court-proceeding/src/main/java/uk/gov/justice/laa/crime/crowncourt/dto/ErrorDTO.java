package uk.gov.justice.laa.crime.crowncourt.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorDTO {
    private String traceId;
    private String code;
    private String message;
}
