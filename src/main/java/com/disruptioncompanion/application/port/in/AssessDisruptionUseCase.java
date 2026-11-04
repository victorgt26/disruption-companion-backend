package com.disruptioncompanion.application.port.in;

import com.disruptioncompanion.domain.model.Assessment;

public interface AssessDisruptionUseCase {
    Assessment execute(String bookingId, boolean compensationRequested);
}
