package com.disruptioncompanion.application.port.out;

import com.disruptioncompanion.domain.model.TripContext;

public interface TripContextPort {
  TripContext getTripContext(String bookingId);
}
