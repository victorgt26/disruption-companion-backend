package com.disruptioncompanion.application.service;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SourceFreshness {
  private final Duration maxAge;

  public boolean isFresh(Instant timestamp, Instant now) {
    return timestamp != null && !timestamp.isAfter(now) && !timestamp.isBefore(now.minus(maxAge));
  }
}
