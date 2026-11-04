package com.disruptioncompanion.application.usecase;

import com.disruptioncompanion.application.port.in.AssessDisruption;
import com.disruptioncompanion.application.port.out.*;
import com.disruptioncompanion.application.service.PolicyEvaluator;
import com.disruptioncompanion.domain.enums.AssessmentStatus;
import com.disruptioncompanion.domain.model.*;
import java.time.*;
import java.util.*;

public final class AssessDisruptionUseCase implements AssessDisruption {
  private final TripContextPort trips;
  private final OperationalEventsPort events;
  private final PolicyRulesPort policies;
  private final Clock clock;
  private final Duration maxAge;
  private final PolicyEvaluator evaluator;

  public AssessDisruptionUseCase(
      TripContextPort trips,
      OperationalEventsPort events,
      PolicyRulesPort policies,
      Clock clock,
      Duration maxAge,
      PolicyEvaluator evaluator) {
    this.trips = trips;
    this.events = events;
    this.policies = policies;
    this.clock = clock;
    this.maxAge = maxAge;
    this.evaluator = evaluator;
  }

  public Assessment assess(String bookingId, boolean compensationRequested) {
    if (bookingId == null || !bookingId.matches("[A-Z0-9]{6}"))
      throw new IllegalArgumentException("Invalid booking identifier");
    Instant now = clock.instant();
    List<String> reasons = new ArrayList<>();
    List<Evidence> evidence = new ArrayList<>();
    List<VerifiedDisruption> disruptions = new ArrayList<>();
    List<Entitlement> entitlements = List.of();
    int delay = 0;
    boolean disruption = false;
    try {
      TripContext trip = trips.getTripContext(bookingId);
      if (trip == null)
        return result(
            disruptions, now, false, evidence, entitlements, List.of("TRIP_NOT_AVAILABLE"));
      if (!bookingId.equals(trip.bookingId())
          || trip.segments() == null
          || trip.segments().isEmpty())
        return result(disruptions, now, false, evidence, entitlements, List.of("TRIP_INCOMPLETE"));
      if (trip._meta() == null || !fresh(trip._meta().retrievedAt(), now))
        reasons.add("TRIP_STALE_OR_UNVERIFIED");
      if (trip._meta() == null
          || !"COMPLETE".equals(trip._meta().completeness())
          || trip._meta().missingFields() != null && !trip._meta().missingFields().isEmpty())
        reasons.add("TRIP_PARTIAL");
      if (trip.passengers() == null
          || trip.passengers().isEmpty()
          || trip.passengers().stream()
              .anyMatch(p -> p == null || p.type() == null && p.age() == null))
        reasons.add("PASSENGERS_UNKNOWN");
      List<String> ids =
          trip.segments().stream()
              .filter(Objects::nonNull)
              .map(Segment::segmentId)
              .filter(Objects::nonNull)
              .toList();
      if (ids.size() != trip.segments().size()
          || ids.stream().anyMatch(String::isBlank)
          || new HashSet<>(ids).size() != ids.size())
        return result(disruptions, now, false, evidence, entitlements, List.of("TRIP_INCOMPLETE"));
      OperationalEvents feed = events.getOperationalEvents(ids);
      if (feed == null
          || feed.events() == null
          || feed._meta() == null
          || !fresh(feed._meta().retrievedAt(), now))
        return result(
            disruptions,
            now,
            false,
            evidence,
            entitlements,
            List.of("EVENTS_UNAVAILABLE_OR_STALE"));
      List<String> degraded =
          feed._meta().sourcesDegraded() == null ? List.of() : feed._meta().sourcesDegraded();
      for (String id : ids) {
        List<OperationalEvent> accepted = new ArrayList<>();
        for (OperationalEvent event : feed.events()) {
          if (event == null || !id.equals(event.segmentId())) continue;
          boolean valid =
              event.eventId() != null
                  && !event.eventId().isBlank()
                  && event.source() != null
                  && fresh(event.recordedAt(), now)
                  && !event.recordedAt().isAfter(feed._meta().retrievedAt())
                  && !degraded.contains(event.source())
                  && feed._meta().sourcesAvailable() != null
                  && feed._meta().sourcesAvailable().contains(event.source());
          String decision = valid ? "CONTEXT_ONLY" : "IGNORED_STALE_DEGRADED_OR_UNVERIFIED";
          if (valid
              && "OPS_CORE".equals(event.source())
              && Set.of("DELAY", "STATUS").contains(Objects.toString(event.type(), ""))) {
            accepted.add(event);
            decision = "AUTHORITATIVE_CANDIDATE";
          }
          evidence.add(
              new Evidence(event.eventId(), id, event.source(), event.recordedAt(), decision));
        }
        if (accepted.isEmpty()) {
          reasons.add("NO_AUTHORITATIVE_EVENT");
          continue;
        }
        Instant latest =
            accepted.stream()
                .map(OperationalEvent::recordedAt)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        List<OperationalEvent> current =
            accepted.stream().filter(e -> e.recordedAt().equals(latest)).toList();
        Set<String> states = new HashSet<>();
        for (OperationalEvent event : current)
          states.add(
              event.type()
                  + ":"
                  + event.status()
                  + ":"
                  + event.delayMinutes()
                  + ":"
                  + event.newDeparture());
        if (states.size() > 1) {
          reasons.add("CONFLICTING_AUTHORITATIVE_EVENTS");
          continue;
        }
        OperationalEvent event = current.getFirst();
        if ("DELAY".equals(event.type())) {
          if (event.delayMinutes() == null
              || event.delayMinutes() < 0
              || event.newDeparture() == null) {
            reasons.add("EVENT_INCOMPLETE");
            continue;
          }
          if (event.delayMinutes() > 0)
            disruptions.add(
                new VerifiedDisruption(
                    id, event.eventId(), event.delayMinutes(), event.newDeparture()));
          delay = Math.max(delay, event.delayMinutes());
          disruption |= event.delayMinutes() > 0;
        } else if (!Set.of("ON_TIME", "SCHEDULED").contains(Objects.toString(event.status(), "")))
          reasons.add("UNSUPPORTED_OPERATIONAL_STATUS");
      }
      if (disruption && ids.size() > 1) reasons.add("CONNECTION_OUTCOME_UNKNOWN");
      boolean minor =
          trip.passengers() != null
              && trip.passengers().stream()
                  .filter(Objects::nonNull)
                  .anyMatch(p -> "CHILD".equals(p.type()) || p.age() != null && p.age() < 18);
      if (compensationRequested && !disruption) reasons.add("COMPENSATION_SCENARIO_UNVERIFIED");
      if (disruption)
        entitlements =
            evaluator.evaluate(
                policies.getPolicyRules("DELAY"),
                delay,
                minor,
                compensationRequested,
                now,
                reasons);
    } catch (SourceUnavailableException ex) {
      reasons.add("SOURCE_UNAVAILABLE");
    }
    return result(disruptions, now, disruption, evidence, entitlements, reasons);
  }

  private boolean fresh(Instant timestamp, Instant now) {
    return timestamp != null && !timestamp.isAfter(now) && !timestamp.isBefore(now.minus(maxAge));
  }

  private Assessment result(
      List<VerifiedDisruption> disruptions,
      Instant now,
      boolean disruption,
      List<Evidence> evidence,
      List<Entitlement> entitlements,
      List<String> reasons) {
    boolean review = !reasons.isEmpty();
    return new Assessment(
        review
            ? AssessmentStatus.REVIEW_REQUIRED
            : disruption ? AssessmentStatus.DISRUPTION : AssessmentStatus.NO_VERIFIED_DISRUPTION,
        now,
        disruption ? "VERIFIED_DELAY" : "NO_CONFIRMED_CHANGE",
        List.copyOf(disruptions),
        List.copyOf(evidence),
        entitlements,
        reasons.stream().distinct().toList(),
        review ? "CONTACT_HUMAN_SUPPORT" : "READ_VERIFIED_INFORMATION");
  }
}
