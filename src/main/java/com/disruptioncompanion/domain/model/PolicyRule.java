package com.disruptioncompanion.domain.model;

import java.time.*;

public record PolicyRule(
    String ruleId, String condition, String entitlement, boolean requiresHumanApproval) {}
