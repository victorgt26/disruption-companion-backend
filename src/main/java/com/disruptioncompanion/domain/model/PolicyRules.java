package com.disruptioncompanion.domain.model;

import java.time.*;
import java.util.List;

public record PolicyRules(
    String scenario,
    String version,
    LocalDate effectiveFrom,
    List<PolicyRule> rules,
    Metadata _meta) {}
