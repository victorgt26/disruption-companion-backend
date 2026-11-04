package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.EvidenceDecision;
import java.time.Instant;

public record Evidence(
    String eventId,
    String segmentId,
    String source,
    Instant recordedAt,
    EvidenceDecision decision) {}
