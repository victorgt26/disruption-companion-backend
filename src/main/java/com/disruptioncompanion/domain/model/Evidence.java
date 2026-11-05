package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.EvidenceDecision;
import com.disruptioncompanion.domain.enums.OperationalSource;

import java.time.Instant;

public record Evidence(
        String eventId,
        String segmentId,
        OperationalSource source,
        Instant recordedAt,
        EvidenceDecision decision) {
}
