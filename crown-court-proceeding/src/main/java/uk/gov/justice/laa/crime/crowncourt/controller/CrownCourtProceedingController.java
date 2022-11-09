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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.laa.crime.crowncourt.dto.CrownCourtsActionsRequestDTO;
import uk.gov.justice.laa.crime.crowncourt.dto.ErrorDTO;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsRequest;
import uk.gov.justice.laa.crime.crowncourt.model.ApiCheckCrownCourtActionsResponse;
import uk.gov.justice.laa.crime.crowncourt.service.CrownCourtProceedingService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/internal/v1/crowncourtproceeding/actions")
@Tag(name = "Crown Court Proceeding", description = "Rest API for Crown Court Proceeding.")
public class CrownCourtProceedingController {

    private final CrownCourtProceedingService crownCourtProceedingService;

    private CrownCourtsActionsRequestDTO preProcessRequest(ApiCheckCrownCourtActionsRequest crownCourtActionsRequest) {
        return new CrownCourtsActionsRequestDTO();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Create an initial means assessment")
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

        CrownCourtsActionsRequestDTO requestDTO = preProcessRequest(crownCourtActionsRequest);
        return ResponseEntity.ok(
                crownCourtProceedingService.checkCrownCourtActions(requestDTO)
        );
    }

}
