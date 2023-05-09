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
import uk.gov.justice.laa.crime.crowncourt.builder.CrownCourtDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.ErrorDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateApplicationResponse;
import uk.gov.justice.laa.crime.crowncourt.service.ProceedingService;

import jakarta.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/proceedings")
@Tag(name = "Crown Court Proceeding", description = "Rest API for Crown Court Proceeding.")
public class CrownCourtProceedingController {

    private final ProceedingService proceedingService;

    private CrownCourtDTO preProcessRequest(ApiProcessRepOrderRequest request) {
        return CrownCourtDTOBuilder.build(request);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Process Rep Order Data")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiProcessRepOrderRequest.class)
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
    public ResponseEntity<ApiProcessRepOrderResponse> processRepOrder(
            @Parameter(description = "Process Crown Rep Order",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiProcessRepOrderRequest.class)
                    )
            ) @Valid @RequestBody ApiProcessRepOrderRequest request) {

        CrownCourtDTO requestDTO = preProcessRequest(request);
        return ResponseEntity.ok(
                proceedingService.processRepOrder(requestDTO)
        );
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Update Crown Court Application")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiUpdateApplicationRequest.class)
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
    public ResponseEntity<ApiUpdateApplicationResponse> updateApplication(@Valid @RequestBody ApiUpdateApplicationRequest request) {
        CrownCourtDTO crownCourtDTO = preProcessRequest(request);
        return ResponseEntity.ok(proceedingService.updateApplication(crownCourtDTO));
    }

    @PostMapping(value = "/graphql", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Retrieve an old means assessment")
    @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Object.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class)))
    @ApiResponse(responseCode = "500", description = "Server Error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorDTO.class)))
    public ResponseEntity<Object> graphQLQuery() throws IOException {
        log.info("Make GraphQL Query Request");
        return ResponseEntity.ok(proceedingService.graphQLQuery());
    }

}
