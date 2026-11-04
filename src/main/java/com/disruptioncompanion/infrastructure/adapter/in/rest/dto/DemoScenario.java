package com.disruptioncompanion.infrastructure.adapter.in.rest.dto;

import com.disruptioncompanion.infrastructure.adapter.out.mock.MockFixtures;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DemoScenario {
  BASE_CASE(new MockFixtures("mocks/trip.json", "mocks/events.json", "mocks/policy.json")),
  NO_DISRUPTION(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/on-time.json",
          "mocks/policy.json")),
  DELAY_BELOW_THRESHOLD(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/delay-179.json",
          "mocks/policy.json")),
  DELAY_AT_THRESHOLD(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/delay-180.json",
          "mocks/policy.json")),
  INTERNATIONAL_UNVERIFIED(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/delay-240.json",
          "mocks/policy.json")),
  MINOR_REQUIRES_APPROVAL(
      new MockFixtures(
          "mocks/demo/trips/minor.json", "mocks/demo/events/delay-160.json", "mocks/policy.json")),
  PARTIAL_TRIP(
      new MockFixtures(
          "mocks/demo/trips/partial.json",
          "mocks/demo/events/delay-160.json",
          "mocks/policy.json")),
  STALE_TRIP(
      new MockFixtures(
          "mocks/demo/trips/stale.json", "mocks/demo/events/delay-160.json", "mocks/policy.json")),
  MISSING_PASSENGERS(
      new MockFixtures(
          "mocks/demo/trips/missing-passengers.json",
          "mocks/demo/events/delay-160.json",
          "mocks/policy.json")),
  INCOMPLETE_TRIP(
      new MockFixtures(
          "mocks/demo/trips/incomplete.json",
          "mocks/demo/events/delay-160.json",
          "mocks/policy.json")),
  CONNECTION_UNVERIFIED(
      new MockFixtures(
          "mocks/demo/trips/connection.json",
          "mocks/demo/events/connection.json",
          "mocks/policy.json")),
  STALE_FEED(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/stale-feed.json",
          "mocks/policy.json")),
  STALE_EVENT(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/stale-event.json",
          "mocks/policy.json")),
  FUTURE_EVENT(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/future-event.json",
          "mocks/policy.json")),
  DEGRADED_SOURCE(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/degraded-source.json",
          "mocks/policy.json")),
  CONFLICTING_EVENTS(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/conflicting.json",
          "mocks/policy.json")),
  INCOMPLETE_EVENT(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/incomplete.json",
          "mocks/policy.json")),
  UNSUPPORTED_STATUS(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/unsupported-status.json",
          "mocks/policy.json")),
  UNKNOWN_POLICY_SCENARIO(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/delay-180.json",
          "mocks/demo/policies/unknown-scenario.json")),
  POLICY_NOT_EFFECTIVE(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/delay-180.json",
          "mocks/demo/policies/future.json")),
  POLICY_SUPERSEDED(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/delay-180.json",
          "mocks/demo/policies/superseded.json")),
  UNKNOWN_POLICY_RULE(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json",
          "mocks/demo/events/delay-180.json",
          "mocks/demo/policies/unknown-rule.json")),
  SOURCE_UNAVAILABLE(
      new MockFixtures(null, "mocks/demo/events/delay-160.json", "mocks/policy.json")),
  POLICY_SOURCE_UNAVAILABLE(
      new MockFixtures(
          "mocks/demo/trips/complete-adult.json", "mocks/demo/events/delay-180.json", null));
  private final MockFixtures fixtures;
}
