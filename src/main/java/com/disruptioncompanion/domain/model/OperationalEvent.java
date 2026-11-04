package com.disruptioncompanion.domain.model;

import com.disruptioncompanion.domain.enums.OperationalEventType;
import com.disruptioncompanion.domain.enums.OperationalSource;
import com.disruptioncompanion.domain.enums.OperationalStatus;
import java.time.Instant;
import java.time.LocalTime;

public record OperationalEvent(
    String eventId,
    String segmentId,
    OperationalEventType type,
    LocalTime newDeparture,
    Integer delayMinutes,
    String gate,
    OperationalStatus status,
    OperationalSource source,
    Instant recordedAt) {}
