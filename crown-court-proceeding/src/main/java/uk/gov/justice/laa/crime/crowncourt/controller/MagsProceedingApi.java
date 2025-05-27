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
import uk.gov.justice.laa.crime.common.model.proceeding.request.ApiDetermineMagsRepDecisionRequest;
import uk.gov.justice.laa.crime.common.model.proceeding.response.ApiDetermineMagsRepDecisionResponse;

public interface MagsProceedingApi {

    @Operation(description = "Determine Magistrates Rep Decision")
    @ApiResponse(
            responseCode = "200",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema =
                                    @Schema(
                                            implementation =
                                                    ApiDetermineMagsRepDecisionResponse.class)))
    @DefaultHTTPErrorResponse
    ResponseEntity<ApiDetermineMagsRepDecisionResponse> determineMagsRepDecision(
            @Parameter(
                            description = "Application Data Required to Determine Rep Decision",
                            content =
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema =
                                                    @Schema(
                                                            implementation =
                                                                    ApiDetermineMagsRepDecisionRequest
                                                                            .class)))
                    @Valid @RequestBody
                    ApiDetermineMagsRepDecisionRequest request);
}
