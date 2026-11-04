package com.disruptioncompanion.application.port.in;

import com.disruptioncompanion.domain.model.Assessment;

public interface AssessDisruption {
  Assessment assess(String bookingId, boolean compensationRequested);
}
