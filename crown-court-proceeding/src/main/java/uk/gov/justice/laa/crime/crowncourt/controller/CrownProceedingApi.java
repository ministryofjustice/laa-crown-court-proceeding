package uk.gov.justice.laa.crime.crowncourt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.justice.laa.crime.annotation.DefaultHTTPErrorResponse;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiProcessRepOrderRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateApplicationRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiUpdateCrownCourtRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiProcessRepOrderResponse;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiUpdateApplicationResponse;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiUpdateCrownCourtOutcomeResponse;
import uk.gov.service.notify.NotificationClientException;


public interface CrownProceedingApi {

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
            ) @Valid @RequestBody ApiUpdateApplicationRequest request)
        throws NotificationClientException, IOException;

    @Operation(description = "Update Crown Court")
    @ApiResponse(responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ApiUpdateCrownCourtOutcomeResponse.class)
            )
    )
    @DefaultHTTPErrorResponse
    ResponseEntity<ApiUpdateCrownCourtOutcomeResponse> updateCrownCourt(
            @Parameter(description = "Updated crown court",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiUpdateCrownCourtRequest.class)
                    )
            ) @Valid @RequestBody ApiUpdateCrownCourtRequest request);
}
