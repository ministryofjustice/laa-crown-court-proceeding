package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.client;

import java.util.UUID;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.dto.HearingResultResponse;

public interface CourtDataAdaptorNonServletApiClient {
  @GetExchange("/hearing_results/{hearingId}")
  void triggerHearingProcessing(@PathVariable UUID hearingId, 
      @RequestParam MultiValueMap<String, String> queryParams);

  @GetExchange("/hearing_results/{hearingId}")
  HearingResultResponse getHearingResult(@PathVariable UUID hearingId,
                                         @RequestParam MultiValueMap<String, String> queryParams);
}
