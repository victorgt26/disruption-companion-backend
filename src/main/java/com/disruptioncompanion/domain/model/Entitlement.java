package com.disruptioncompanion.domain.model;

import java.time.*;

public record Entitlement(
    String code, String policyRef, String ruleId, boolean requiresHumanApproval) {}
