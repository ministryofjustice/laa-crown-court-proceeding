package uk.gov.justice.laa.crime.crowncourt.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.crime.crowncourt.builder.CrownCourtDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.model.request.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.crowncourt.model.request.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiUpdateApplicationResponse;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.justice.laa.crime.crowncourt.service.ProceedingService;
import uk.gov.justice.laa.crime.crowncourt.validation.CrownCourtDetailsValidator;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/proceedings")
@Tag(name = "Crown Court Proceeding", description = "Rest API for Crown Court Proceeding.")
public class ProceedingsController implements ProceedingsApi {

    private final CrownProceedingService crownProceedingService;
    private final CrownCourtDetailsValidator crownCourtDetailsValidator;

    private CrownCourtDTO preProcessRequest(ApiProcessRepOrderRequest request) {
        return CrownCourtDTOBuilder.build(request);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiProcessRepOrderResponse> processRepOrder(ApiProcessRepOrderRequest request) {
        CrownCourtDTO requestDTO = preProcessRequest(request);
        return ResponseEntity.ok(
                proceedingService.processRepOrder(requestDTO)
        );
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiUpdateApplicationResponse> updateApplication(ApiUpdateApplicationRequest request) {
        CrownCourtDTO crownCourtDTO = preProcessRequest(request);
        return ResponseEntity.ok(proceedingService.updateApplication(crownCourtDTO));
    }

    @PutMapping(value = "/update-crown-court", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiUpdateCrownCourtOutcomeResponse> update(ApiUpdateApplicationRequest request) {
        CrownCourtDTO crownCourtDTO = preProcessRequest(request);
        crownCourtDetailsValidator.checkCCDetails(crownCourtDTO);
        return ResponseEntity.ok(proceedingService.update(crownCourtDTO));
    }
}
