package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.EntitlementCode;

public record Entitlement(
    EntitlementCode code, String policyRef, String ruleId, boolean requiresHumanApproval) {}
