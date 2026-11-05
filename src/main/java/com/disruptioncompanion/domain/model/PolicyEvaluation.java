package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.ReviewReason;

import java.util.List;

public record PolicyEvaluation(List<Entitlement> entitlements, List<ReviewReason> reviewReasons) {
}
