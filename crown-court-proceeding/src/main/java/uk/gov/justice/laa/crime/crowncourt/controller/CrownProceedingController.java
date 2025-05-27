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
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateCrownCourtRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiUpdateApplicationResponse;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.justice.laa.crime.crowncourt.builder.CrownCourtDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.service.CrownProceedingService;
import uk.gov.justice.laa.crime.crowncourt.validation.CrownCourtDetailsValidator;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/proceedings")
@Tag(name = "Crown Court Proceeding", description = "Rest API for Crown Court Proceeding.")
public class CrownProceedingController implements CrownProceedingApi {

    private final CrownProceedingService crownProceedingService;
    private final CrownCourtDetailsValidator crownCourtDetailsValidator;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiProcessRepOrderResponse> processRepOrder(
            ApiProcessRepOrderRequest request) {
        CrownCourtDTO requestDTO = CrownCourtDTOBuilder.build(request);
        return ResponseEntity.ok(crownProceedingService.processRepOrder(requestDTO));
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiUpdateApplicationResponse> updateApplication(
            ApiUpdateApplicationRequest request) {
        CrownCourtDTO crownCourtDTO = CrownCourtDTOBuilder.build(request);
        return ResponseEntity.ok(crownProceedingService.updateApplication(crownCourtDTO));
    }

    @PutMapping(value = "/update-crown-court", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiUpdateCrownCourtOutcomeResponse> updateCrownCourt(
            ApiUpdateCrownCourtRequest request) {
        CrownCourtDTO crownCourtDTO = CrownCourtDTOBuilder.build(request);
        crownCourtDetailsValidator.checkCCDetails(crownCourtDTO);
        return ResponseEntity.ok(crownProceedingService.update(crownCourtDTO));
    }
}
