package com.disruptioncompanion.domain.model;

import java.time.*;
import java.util.List;

public record TripContext(
    String bookingId,
    List<Passenger> passengers,
    List<Segment> segments,
    FareConditions fareConditions,
    Metadata _meta) {}
