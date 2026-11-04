package com.disruptioncompanion.domain.model;

import java.time.*;
import java.util.List;

public record Metadata(
    Instant retrievedAt,
    String completeness,
    List<String> missingFields,
    List<String> sourcesAvailable,
    List<String> sourcesDegraded,
    List<String> supersededVersions) {}
