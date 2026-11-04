package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.AssessmentMessage;
import com.disruptioncompanion.domain.enums.AssessmentStatus;
import com.disruptioncompanion.domain.enums.NextStep;
import com.disruptioncompanion.domain.enums.ReviewReason;

import java.time.Instant;
import java.util.List;

public record Assessment(
        AssessmentStatus status,
        Instant assessedAt,
        AssessmentMessage messageCode,
        List<VerifiedDisruption> disruptions,
        List<Evidence> evidence,
        List<Entitlement> entitlements,
        List<ReviewReason> reviewReasons,
        NextStep nextStep) {
}
