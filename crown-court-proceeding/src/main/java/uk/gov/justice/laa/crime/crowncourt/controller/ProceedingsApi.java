package uk.gov.justice.laa.crime.crowncourt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.justice.laa.crime.annotation.DefaultHTTPErrorResponse;
import uk.gov.justice.laa.crime.crowncourt.model.request.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.crowncourt.model.request.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiUpdateApplicationResponse;
import uk.gov.justice.laa.crime.crowncourt.model.response.ApiUpdateCrownCourtOutcomeResponse;

public interface ProceedingsApi {

    @Operation(description = "Process Rep Order")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiProcessRepOrderResponse.class)
            )
    )
    @DefaultHTTPErrorResponse
    ResponseEntity<ApiProcessRepOrderResponse> processRepOrder(
            @Parameter(description = "Process Crown Rep Order Data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiProcessRepOrderRequest.class)
                    )
            ) @Valid @RequestBody ApiProcessRepOrderRequest request);




    @Operation(description = "Update Crown Court Application")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiUpdateApplicationResponse.class)
            )
    )
    @DefaultHTTPErrorResponse
    ResponseEntity<ApiUpdateApplicationResponse> updateApplication(
            @Parameter(description = "Updated Application Data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiUpdateApplicationRequest.class)
                    )
            ) @Valid @RequestBody ApiUpdateApplicationRequest request);




    @Operation(description = "Update Crown Court")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiUpdateApplicationResponse.class)
            )
    )
    @DefaultHTTPErrorResponse
    ResponseEntity<ApiUpdateCrownCourtOutcomeResponse> update(
            @Parameter(description = "Updated Application Data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiUpdateApplicationRequest.class)
                    )
            ) @Valid @RequestBody ApiUpdateApplicationRequest request);

}
