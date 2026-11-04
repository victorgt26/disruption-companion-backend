package com.disruptioncompanion.infrastructure.adapter.in.rest.dto;

import com.disruptioncompanion.domain.model.*;
import java.time.Instant;
import java.util.List;

public record AssessmentResponse(
    String status,
    Instant assessedAt,
    String messageCode,
    List<VerifiedDisruption> disruptions,
    List<Evidence> evidence,
    List<Entitlement> entitlements,
    List<String> reviewReasons,
    String nextStep) {}
