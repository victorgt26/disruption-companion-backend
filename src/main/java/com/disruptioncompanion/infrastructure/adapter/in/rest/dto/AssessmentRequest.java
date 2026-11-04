package com.disruptioncompanion.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AssessmentRequest(
        @Schema(description = "Booking identifier for the local demo", example = "ABC123")
        @NotBlank
        @Pattern(regexp = "[A-Z0-9]{6}")
        String bookingId,
        @Schema(
                description = "Requests a compensation assessment, never an automatic payment",
                example = "false")
        Boolean compensationRequested) {
}
