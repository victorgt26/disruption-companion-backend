package com.disruptioncompanion.infrastructure.adapter.out.mock;

import com.disruptioncompanion.application.port.out.OperationalEventsPort;
import com.disruptioncompanion.application.port.out.PolicyRulesPort;
import com.disruptioncompanion.application.port.out.SourceUnavailableException;
import com.disruptioncompanion.application.port.out.TripContextPort;
import com.disruptioncompanion.domain.enums.DisruptionScenario;
import com.disruptioncompanion.domain.model.OperationalEvents;
import com.disruptioncompanion.domain.model.PolicyRules;
import com.disruptioncompanion.domain.model.TripContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public final class JsonMockAdapter
        implements TripContextPort, OperationalEventsPort, PolicyRulesPort {
    private final ObjectMapper mapper;
    private final MockFixtures fixtures;

    private <T> T read(String file, Class<T> type) {
        if (file == null) {
            throw new SourceUnavailableException();
        }
        try (var input = new ClassPathResource(file).getInputStream()) {
            return mapper.readValue(input, type);
        } catch (IOException | RuntimeException ex) {
            throw new SourceUnavailableException();
        }
    }

    public TripContext getTripContext(String bookingId) {
        TripContext trip = read(fixtures.tripResource(), TripContext.class);
        return trip.bookingId().equals(bookingId) ? trip : null;
    }

    public OperationalEvents getOperationalEvents(List<String> segmentIds) {
        OperationalEvents feed = read(fixtures.eventsResource(), OperationalEvents.class);
        return new OperationalEvents(
                feed.events().stream().filter(e -> segmentIds.contains(e.segmentId())).toList(),
                feed._meta());
    }

    public PolicyRules getPolicyRules(DisruptionScenario scenario) {
        PolicyRules policy = read(fixtures.policyResource(), PolicyRules.class);
        return policy.scenario() == scenario ? policy : null;
    }
}
