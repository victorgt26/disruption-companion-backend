package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.ReviewReason;
import java.util.List;

public record OperationalAssessment(
    List<VerifiedDisruption> disruptions,
    List<Evidence> evidence,
    List<ReviewReason> reviewReasons) {}
