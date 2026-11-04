package com.disruptioncompanion.domain.model;

import java.time.Instant;
import java.time.LocalTime;

public record OperationalEvent(
    String eventId,
    String segmentId,
    String type,
    LocalTime newDeparture,
    Integer delayMinutes,
    String gate,
    String status,
    String source,
    Instant recordedAt) {}
