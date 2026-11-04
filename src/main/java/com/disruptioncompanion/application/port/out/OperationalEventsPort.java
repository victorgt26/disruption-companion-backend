package com.disruptioncompanion.application.port.out;

import com.disruptioncompanion.domain.model.*;

public interface OperationalEventsPort {
  OperationalEvents getOperationalEvents(java.util.List<String> segmentIds);
}
