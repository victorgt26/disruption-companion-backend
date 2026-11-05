package com.disruptioncompanion.application.port.out;

import com.disruptioncompanion.domain.model.OperationalEvents;

public interface OperationalEventsPort {
    OperationalEvents getOperationalEvents(java.util.List<String> segmentIds);
}
