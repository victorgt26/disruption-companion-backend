package com.disruptioncompanion.domain.model;

import java.time.*;

public record Segment(
    String segmentId,
    String flightNumber,
    String origin,
    String destination,
    LocalDate date,
    LocalTime scheduledDeparture,
    LocalTime scheduledArrival) {}
