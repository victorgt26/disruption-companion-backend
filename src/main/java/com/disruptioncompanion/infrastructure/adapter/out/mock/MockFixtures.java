package com.disruptioncompanion.infrastructure.adapter.out.mock;

public record MockFixtures(String tripResource, String eventsResource, String policyResource) {
  public static MockFixtures standard() {
    return new MockFixtures("mocks/trip.json", "mocks/events.json", "mocks/policy.json");
  }
}
