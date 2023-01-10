package uk.gov.justice.laa.crime.crowncourt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.laa.crime.crowncourt.builder.ProcessCrownRepOrderRequestDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.builder.CrownCourtApplicationRequestDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.ProcessCrownRepOrderRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.ErrorDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessCrownRepOrderRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessCrownRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateCrownCourtApplicationRequest;
import uk.gov.justice.laa.crime.crowncourt.service.CrownCourtProceedingService;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/proceedings")
@Tag(name = "Crown Court Proceeding", description = "Rest API for Crown Court Proceeding.")
public class CrownCourtProceedingController {

    private final CrownCourtProceedingService crownCourtProceedingService;

    private ProcessCrownRepOrderRequestDTO preProcessRequest(ApiProcessCrownRepOrderRequest processCrownRepOrderRequest) {
        return new ProcessCrownRepOrderRequestDTOBuilder().buildRequestDTO(processCrownRepOrderRequest);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Process Crown Rep Order Data")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiProcessCrownRepOrderRequest.class)
            )
    )
    @ApiResponse(responseCode = "400",
            description = "Bad Request.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
            )
    )
    @ApiResponse(responseCode = "500",
            description = "Server Error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
            )
    )
    public ResponseEntity<ApiProcessCrownRepOrderResponse> processCrownRepOrder(
            @Parameter(description = "Process Crown Rep Order",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiProcessCrownRepOrderRequest.class)
                    )
            ) @Valid @RequestBody ApiProcessCrownRepOrderRequest processCrownRepOrderRequest) {

        ProcessCrownRepOrderRequestDTO requestDTO = preProcessRequest(processCrownRepOrderRequest);
        return ResponseEntity.ok(
                crownCourtProceedingService.processCrownRepOrder(requestDTO)
        );
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Update Crown Court Application")
    @ApiResponse(responseCode = "200", content = @Content())
    @ApiResponse(responseCode = "400",
            description = "Bad Request.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
            )
    )
    @ApiResponse(responseCode = "500",
            description = "Server Error.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ErrorDTO.class)
            )
    )
    public ResponseEntity<Object> updateCrownCourtActions(@Valid @RequestBody ApiUpdateCrownCourtApplicationRequest crownCourtApplicationRequest) {
        crownCourtProceedingService.updateCrownCourtApplication(
                new CrownCourtApplicationRequestDTOBuilder().buildRequestDTO(crownCourtApplicationRequest));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/graphql", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Retrieve an old means assessment")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "500", description = "Server Error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<Object> graphQLQuery() throws URISyntaxException, IOException {
        log.info("Make GraphQL Query Request");
        return ResponseEntity.ok(crownCourtProceedingService.graphQLQuery());
    }

}
