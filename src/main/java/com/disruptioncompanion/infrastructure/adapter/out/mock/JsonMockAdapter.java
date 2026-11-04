package com.disruptioncompanion.infrastructure.adapter.out.mock;

import com.disruptioncompanion.application.port.out.*;
import com.disruptioncompanion.domain.model.*;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

public final class JsonMockAdapter
    implements TripContextPort, OperationalEventsPort, PolicyRulesPort {
  private final ObjectMapper mapper;

  public JsonMockAdapter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  private <T> T read(String file, Class<T> type) {
    try (var input = new ClassPathResource("mocks/" + file + ".json").getInputStream()) {
      return mapper.readValue(input, type);
    } catch (IOException | RuntimeException ex) {
      throw new SourceUnavailableException();
    }
  }

  public TripContext getTripContext(String bookingId) {
    TripContext trip = read("trip", TripContext.class);
    return trip.bookingId().equals(bookingId) ? trip : null;
  }

  public OperationalEvents getOperationalEvents(List<String> segmentIds) {
    OperationalEvents feed = read("events", OperationalEvents.class);
    return new OperationalEvents(
        feed.events().stream().filter(e -> segmentIds.contains(e.segmentId())).toList(),
        feed._meta());
  }

  public PolicyRules getPolicyRules(String scenario) {
    PolicyRules policy = read("policy", PolicyRules.class);
    return policy.scenario().equals(scenario) ? policy : null;
  }
}
