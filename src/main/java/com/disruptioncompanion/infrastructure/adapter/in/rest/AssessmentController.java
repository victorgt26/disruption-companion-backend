package com.disruptioncompanion.infrastructure.adapter.in.rest;

import com.disruptioncompanion.application.port.in.AssessDisruptionUseCase;
import com.disruptioncompanion.infrastructure.adapter.in.rest.dto.AssessmentRequest;
import com.disruptioncompanion.infrastructure.adapter.in.rest.dto.AssessmentResponse;
import com.disruptioncompanion.infrastructure.adapter.in.rest.mapper.AssessmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {
    private final AssessDisruptionUseCase assessDisruption;

    @Operation(
            summary = "Assess a trip disruption",
            description =
                    "Read-only assessment. Review states and policy matches do not authorize a booking change"
                            + " or compensation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessment, possibly requiring human review"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected assessment failure",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<AssessmentResponse> assess(@Valid @RequestBody AssessmentRequest request) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(
                        AssessmentMapper.toResponse(
                                assessDisruption.execute(request.bookingId(), Boolean.TRUE.equals(request.compensationRequested()))
                        )
                );
    }
}
