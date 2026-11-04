package com.disruptioncompanion.domain.model;

public record PolicyRule(
    String ruleId, String condition, String entitlement, boolean requiresHumanApproval) {}
