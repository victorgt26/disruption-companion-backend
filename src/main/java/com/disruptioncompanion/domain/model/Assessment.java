package com.disruptioncompanion.domain.model;

import java.time.*;
import java.util.List;

public record Assessment(
    com.disruptioncompanion.domain.enums.AssessmentStatus status,
    Instant assessedAt,
    String messageCode,
    List<VerifiedDisruption> disruptions,
    List<Evidence> evidence,
    List<Entitlement> entitlements,
    List<String> reviewReasons,
    String nextStep) {}
