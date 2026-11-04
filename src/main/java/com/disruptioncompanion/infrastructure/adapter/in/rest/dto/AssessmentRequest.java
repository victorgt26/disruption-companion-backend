package com.disruptioncompanion.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.*;

public record AssessmentRequest(
    @NotBlank @Pattern(regexp = "[A-Z0-9]{6}") String bookingId, Boolean compensationRequested) {}
