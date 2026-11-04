package com.disruptioncompanion.infrastructure.adapter.in.rest;

import com.disruptioncompanion.infrastructure.adapter.in.rest.dto.AssessmentResponse;
import com.disruptioncompanion.infrastructure.adapter.in.rest.dto.DemoAssessmentRequest;
import com.disruptioncompanion.infrastructure.adapter.in.rest.mapper.AssessmentMapper;
import com.disruptioncompanion.infrastructure.config.AssessmentUseCaseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("demo")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/demo/assessments")
public class DemoAssessmentController {
    private final AssessmentUseCaseFactory factory;

    @Operation(
            summary = "Try assessment fixtures (demo only)",
            description =
                    "Choose a named request example to execute the real use case with isolated JSON fixtures."
                            + " No booking changes or human cases are created.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assessment computed from selected fixtures"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or demo selector",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected assessment failure",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<AssessmentResponse> assess(@Valid @RequestBody DemoAssessmentRequest body) {
        var useCase = factory.create(body.scenario().getFixtures());
        var request = body.request();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(AssessmentMapper.toResponse(
                        useCase.execute(request.bookingId(), Boolean.TRUE.equals(request.compensationRequested()))
                ));
    }
}
