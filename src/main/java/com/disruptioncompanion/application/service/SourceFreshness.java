package com.disruptioncompanion.application.service;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
public final class SourceFreshness {
    private final Duration maxAge;

    public boolean isFresh(Instant timestamp, Instant now) {
        return timestamp != null && !timestamp.isAfter(now) && !timestamp.isBefore(now.minus(maxAge));
    }
}
