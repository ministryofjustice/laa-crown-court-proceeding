package uk.gov.justice.laa.crime.crowncourt.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.crime.crowncourt.builder.CrownCourtDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.model.MagsDecisionResult;
import uk.gov.justice.laa.crime.crowncourt.model.request.ApiDetermineMagsRepDecisionRequest;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiDetermineMagsRepDecisionResponse;
import uk.gov.justice.laa.crime.crowncourt.service.MagsProceedingService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/proceedings")
@Tag(name = "Magistrate Court Proceedings", description = "Rest API for Magistrate Court Proceedings")
public class MagsProceedingController implements MagsProceedingApi {

    private final MagsProceedingService magsProceedingService;

    @PostMapping(value = "/determine-mags-rep-decision", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiDetermineMagsRepDecisionResponse> determineMagsRepDecision(
            ApiDetermineMagsRepDecisionRequest request) {

        CrownCourtDTO crownCourtDTO = CrownCourtDTOBuilder.build(request);
        MagsDecisionResult decisionResult = magsProceedingService.determineMagsRepDecision(crownCourtDTO);
        ApiDetermineMagsRepDecisionResponse response = new ApiDetermineMagsRepDecisionResponse();

        if (decisionResult != null) {
            response.withDecisionResult(decisionResult);
        }
        return ResponseEntity.ok(response);
    }
}
