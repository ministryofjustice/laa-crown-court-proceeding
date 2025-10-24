package uk.gov.justice.laa.crime.crowncourt.client;

import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.IOJAppealDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderCCOutcomeDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.RepOrderDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.maatcourtdata.UpdateRepOrderRequestDTO;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange()
public interface MaatCourtDataApiClient {

    @GetExchange("/ioj-appeal/repId/{repId}/current-passed")
    IOJAppealDTO getCurrentPassedIOJAppeal(@PathVariable Integer repId);

    @PutExchange("/rep-orders")
    RepOrderDTO updateRepOrder(@RequestBody UpdateRepOrderRequestDTO request);

    @GetExchange("/rep-orders/cc-outcome/reporder/{repId}")
    List<RepOrderCCOutcomeDTO> getRepOrderCCOutcomeByRepId(@PathVariable Integer repId);

    @PostExchange("/rep-orders/cc-outcome")
    RepOrderCCOutcomeDTO createCrownCourtOutcome(@RequestBody RepOrderCCOutcomeDTO request);

    @HttpExchange(method = "HEAD", url = "/rep-orders/cc-outcome/reporder/{repId}")
    ResponseEntity<Void> getOutcomeCount(@PathVariable Integer repId);
}
