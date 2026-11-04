package com.disruptioncompanion.domain.model;

import java.time.*;

public record Evidence(
    String eventId, String segmentId, String source, Instant recordedAt, String decision) {}
