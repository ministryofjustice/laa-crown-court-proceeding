package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.model.Result;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingResult {

    private List<Result> result;
}
