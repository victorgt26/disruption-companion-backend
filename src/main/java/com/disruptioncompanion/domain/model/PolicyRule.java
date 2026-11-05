package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.EntitlementCode;

public record PolicyRule(
        String ruleId,
        String condition,
        EntitlementCode entitlement,
        boolean requiresHumanApproval) {
}
