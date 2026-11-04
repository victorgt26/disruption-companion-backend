package com.disruptioncompanion.infrastructure.adapter.in.rest.mapper;

import com.disruptioncompanion.domain.model.Assessment;
import com.disruptioncompanion.infrastructure.adapter.in.rest.dto.AssessmentResponse;

public final class AssessmentMapper {
  private AssessmentMapper() {}

  public static AssessmentResponse toResponse(Assessment result) {
    return new AssessmentResponse(
        result.status(),
        result.assessedAt(),
        result.messageCode(),
        result.disruptions(),
        result.evidence(),
        result.entitlements(),
        result.reviewReasons(),
        result.nextStep());
  }
}
