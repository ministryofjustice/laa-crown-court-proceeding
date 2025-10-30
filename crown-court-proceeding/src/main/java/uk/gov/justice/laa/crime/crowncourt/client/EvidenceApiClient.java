package uk.gov.justice.laa.crime.crowncourt.client;

import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiCalculateEvidenceFeeRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiCalculateEvidenceFeeResponse;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface EvidenceApiClient {

    @PostExchange("/calculate-evidence-fee")
    ApiCalculateEvidenceFeeResponse calculateEvidenceFee(@RequestBody ApiCalculateEvidenceFeeRequest request);
}
