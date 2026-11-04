package com.disruptioncompanion.infrastructure.adapter.in.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record DemoAssessmentRequest(
        @NotNull DemoScenario scenario, @Valid @NotNull AssessmentRequest request) {
}
