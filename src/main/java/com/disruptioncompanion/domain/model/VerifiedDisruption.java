package com.disruptioncompanion.domain.model;

import java.time.LocalTime;

public record VerifiedDisruption(
        String segmentId,
        String eventId,
        int delayMinutes,
        LocalTime newDeparture) {
}
