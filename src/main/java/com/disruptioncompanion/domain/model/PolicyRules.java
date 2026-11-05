package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.DisruptionScenario;

import java.time.LocalDate;
import java.util.List;

public record PolicyRules(
        DisruptionScenario scenario,
        String version,
        LocalDate effectiveFrom,
        List<PolicyRule> rules,
        Metadata _meta) {
}
