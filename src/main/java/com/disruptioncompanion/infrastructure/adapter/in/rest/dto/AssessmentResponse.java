package com.disruptioncompanion.infrastructure.adapter.in.rest.dto;

import com.disruptioncompanion.domain.enums.AssessmentMessage;
import com.disruptioncompanion.domain.enums.AssessmentStatus;
import com.disruptioncompanion.domain.enums.NextStep;
import com.disruptioncompanion.domain.enums.ReviewReason;
import com.disruptioncompanion.domain.model.Entitlement;
import com.disruptioncompanion.domain.model.Evidence;
import com.disruptioncompanion.domain.model.VerifiedDisruption;

import java.time.Instant;
import java.util.List;

public record AssessmentResponse(
        AssessmentStatus status,
        Instant assessedAt,
        AssessmentMessage messageCode,
        List<VerifiedDisruption> disruptions,
        List<Evidence> evidence,
        List<Entitlement> entitlements,
        List<ReviewReason> reviewReasons,
        NextStep nextStep) {
}
