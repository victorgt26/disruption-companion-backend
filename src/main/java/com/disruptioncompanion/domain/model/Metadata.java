package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.DataCompleteness;
import com.disruptioncompanion.domain.enums.OperationalSource;
import java.time.Instant;
import java.util.List;

public record Metadata(
    Instant retrievedAt,
    DataCompleteness completeness,
    List<String> missingFields,
    List<OperationalSource> sourcesAvailable,
    List<OperationalSource> sourcesDegraded,
    List<String> supersededVersions) {}
