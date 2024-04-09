package uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.laa.crime.crowncourt.prosecution_concluded.service.ProsecutionConcludedDataService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/proceedings/prosecution/scheduler")
@Tag(name = "Prosecution concluded", description = "Rest API for Prosecution concluded")
public class ProsecutionConcludedController {

    private final ProsecutionConcludedDataService prosecutionConcludedDataService;

    @RequestMapping(value = "/{maatId}",
            method = {RequestMethod.HEAD},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(description = "Retrieve Prosecution Concluded Count")
    public ResponseEntity<Object> getCountByMaatIdAndStatus(@PathVariable int maatId,
                                                            @RequestParam(value = "status", defaultValue = "PENDING") String  status) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentLength(prosecutionConcludedDataService.getCountByMaatIdAndStatus(maatId, status));
        return ResponseEntity.ok().headers(responseHeaders).build();
    }
}
