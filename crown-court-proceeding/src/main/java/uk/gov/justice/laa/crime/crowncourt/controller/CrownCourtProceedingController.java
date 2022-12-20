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
import uk.gov.justice.laa.crime.crowncourt.builder.CrownCourtActionsRequestDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.builder.CrownCourtApplicationRequestDTOBuilder;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.ErrorDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsResponse;
import uk.gov.justice.laa.crime.crowncourt.model.ApiUpdateCrownCourtApplicationRequest;
import uk.gov.justice.laa.crime.crowncourt.service.CrownCourtProceedingService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/crowncourtproceeding/actions")
@Tag(name = "Crown Court Proceeding", description = "Rest API for Crown Court Proceeding.")
public class CrownCourtProceedingController {

    private final CrownCourtProceedingService crownCourtProceedingService;

    private CrownCourtActionsRequestDTO preProcessRequest(ApiCheckCrownCourtActionsRequest crownCourtActionsRequest) {
        return new CrownCourtActionsRequestDTOBuilder().buildRequestDTO(crownCourtActionsRequest);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Check Crown Court Actions data")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiCheckCrownCourtActionsResponse.class)
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
    public ResponseEntity<ApiCheckCrownCourtActionsResponse> checkCrownCourtActions(@Parameter(description = "Check Crown Court Actions data",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiCheckCrownCourtActionsRequest.class)
            )
    ) @Valid @RequestBody ApiCheckCrownCourtActionsRequest crownCourtActionsRequest) {

        CrownCourtActionsRequestDTO requestDTO = preProcessRequest(crownCourtActionsRequest);
        return ResponseEntity.ok(
                crownCourtProceedingService.checkCrownCourtActions(requestDTO)
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

}
