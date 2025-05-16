package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/proceedings/prosecution-concluded/{maatId}/messages")
@Tag(name = "Prosecution concluded", description = "Rest API for prosecution concluded messages")
public class ProsecutionConcludedController {

    private final ProsecutionConcludedDataService concludedDataService;

    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Retrieve Prosecution Concluded Count")
    public ResponseEntity<Object> getCountByMaatIdAndStatus(
            @PathVariable int maatId,
            @RequestParam(value = "status", defaultValue = "PENDING") String status) {
        return ResponseEntity.ok(concludedDataService.getCountByMaatIdAndStatus(maatId, status));
    }
}
