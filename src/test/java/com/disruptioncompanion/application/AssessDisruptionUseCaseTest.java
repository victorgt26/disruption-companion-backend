package com.disruptioncompanion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.disruptioncompanion.application.port.out.*;
import com.disruptioncompanion.application.service.PolicyEvaluator;
import com.disruptioncompanion.application.usecase.AssessDisruptionUseCase;
import com.disruptioncompanion.domain.enums.AssessmentStatus;
import com.disruptioncompanion.domain.model.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssessDisruptionUseCaseTest {
  static final Instant NOW = Instant.parse("2026-09-15T11:03:00Z");
  @Mock TripContextPort trips;
  @Mock OperationalEventsPort events;
  @Mock PolicyRulesPort policies;
  AssessDisruptionUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new AssessDisruptionUseCase(
            trips,
            events,
            policies,
            Clock.fixed(NOW, ZoneOffset.UTC),
            Duration.ofMinutes(30),
            new PolicyEvaluator());
  }

  Metadata meta(String completeness) {
    return new Metadata(
        NOW.minusSeconds(30),
        completeness,
        List.of(),
        List.of("OPS_CORE", "LEGACY_MIRROR"),
        List.of("LEGACY_MIRROR"),
        List.of("OLD"));
  }

  TripContext trip(boolean partial, boolean minor, boolean connection) {
    Segment s1 =
        new Segment(
            "S1",
            "LA500",
            "SCL",
            "LIM",
            LocalDate.parse("2026-09-15"),
            LocalTime.of(14, 30),
            LocalTime.of(17, 55));
    Segment s2 =
        new Segment(
            "S2", "LA2410", "LIM", "MEX", s1.date(), LocalTime.of(19, 5), LocalTime.of(23, 40));
    return new TripContext(
        "ABC123",
        List.of(
            new Passenger("P1", "ADULT", null),
            new Passenger("P2", minor ? "CHILD" : "ADULT", minor ? 7 : null)),
        connection ? List.of(s1, s2) : List.of(s1),
        new FareConditions(true, false, "LIGHT"),
        meta(partial ? "PARTIAL" : "COMPLETE"));
  }

  OperationalEvent delay(int minutes, Instant at) {
    return new OperationalEvent(
        "E1", "S1", "DELAY", LocalTime.of(17, 10), minutes, null, null, "OPS_CORE", at);
  }

  PolicyRules policy() {
    return new PolicyRules(
        "DELAY",
        "POL-DELAY-2026-03",
        LocalDate.parse("2026-03-01"),
        List.of(
            new PolicyRule("R1", "delay_minutes >= 180", "REBOOK_NO_FEE", false),
            new PolicyRule("R2", "delay_minutes >= 240 AND international", "MEAL_VOUCHER", false),
            new PolicyRule("R3", "missed_connection == true", "REBOOK_NO_FEE", false),
            new PolicyRule("R4", "compensation_requested == true", "CASH_COMPENSATION", true),
            new PolicyRule("R5", "group_includes_minor == true", "PRIORITY_HANDLING", true)),
        meta("COMPLETE"));
  }

  void givenDelay(int minutes, boolean partial, boolean minor, boolean connection) {
    when(trips.getTripContext("ABC123")).thenReturn(trip(partial, minor, connection));
    var list = new ArrayList<OperationalEvent>();
    list.add(delay(minutes, NOW.minusSeconds(900)));
    list.add(
        new OperationalEvent(
            "OLD",
            "S1",
            "STATUS",
            null,
            null,
            null,
            "ON_TIME",
            "LEGACY_MIRROR",
            NOW.minusSeconds(5400)));
    if (connection)
      list.add(
          new OperationalEvent(
              "E2",
              "S2",
              "STATUS",
              null,
              null,
              null,
              "SCHEDULED",
              "OPS_CORE",
              NOW.minusSeconds(900)));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(new OperationalEvents(list, meta("COMPLETE")));
    when(policies.getPolicyRules("DELAY")).thenReturn(policy());
  }

  @Test
  void historicalBaseCaseDoesNotInventFreeRebookingOrMissedConnection() {
    givenDelay(160, true, true, true);
    Assessment result = useCase.assess("ABC123", false);
    assertEquals(AssessmentStatus.REVIEW_REQUIRED, result.status());
    assertEquals(160, result.disruptions().getFirst().delayMinutes());
    assertEquals(
        List.of("PRIORITY_HANDLING"),
        result.entitlements().stream().map(Entitlement::code).toList());
    assertTrue(result.entitlements().getFirst().requiresHumanApproval());
    assertTrue(
        result
            .reviewReasons()
            .containsAll(
                List.of("TRIP_PARTIAL", "CONNECTION_OUTCOME_UNKNOWN", "HUMAN_APPROVAL_REQUIRED")));
    assertTrue(
        result.evidence().stream()
            .anyMatch(e -> e.eventId().equals("OLD") && e.decision().startsWith("IGNORED")));
  }

  @ParameterizedTest
  @CsvSource({"179,false", "180,true", "239,true", "240,true"})
  void rebookingThresholdIsInclusive(int delay, boolean expected) {
    givenDelay(delay, false, false, false);
    Assessment result = useCase.assess("ABC123", false);
    assertEquals(
        expected, result.entitlements().stream().anyMatch(e -> e.code().equals("REBOOK_NO_FEE")));
    assertTrue(result.entitlements().stream().noneMatch(e -> e.code().equals("MEAL_VOUCHER")));
    if (delay >= 240) assertTrue(result.reviewReasons().contains("INTERNATIONAL_STATUS_UNKNOWN"));
  }

  @Test
  void compensationAlwaysRequiresHumanApproval() {
    givenDelay(160, false, false, false);
    var result = useCase.assess("ABC123", true);
    assertTrue(
        result.entitlements().stream()
            .anyMatch(e -> e.code().equals("CASH_COMPENSATION") && e.requiresHumanApproval()));
    assertEquals("CONTACT_HUMAN_SUPPORT", result.nextStep());
  }

  @Test
  void staleEventsCannotTriggerEntitlements() {
    when(trips.getTripContext("ABC123")).thenReturn(trip(false, false, false));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(
            new OperationalEvents(List.of(delay(300, NOW.minusSeconds(1801))), meta("COMPLETE")));
    var result = useCase.assess("ABC123", false);
    assertTrue(result.disruptions().isEmpty());
    assertTrue(result.entitlements().isEmpty());
    assertTrue(result.reviewReasons().contains("NO_AUTHORITATIVE_EVENT"));
    verifyNoInteractions(policies);
  }

  @Test
  void conflictingAuthoritativeEventsRequireReview() {
    when(trips.getTripContext("ABC123")).thenReturn(trip(false, false, false));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(
            new OperationalEvents(
                List.of(
                    delay(180, NOW.minusSeconds(60)),
                    new OperationalEvent(
                        "E2",
                        "S1",
                        "STATUS",
                        null,
                        null,
                        null,
                        "ON_TIME",
                        "OPS_CORE",
                        NOW.minusSeconds(60))),
                meta("COMPLETE")));
    var result = useCase.assess("ABC123", false);
    assertTrue(result.reviewReasons().contains("CONFLICTING_AUTHORITATIVE_EVENTS"));
    assertTrue(result.entitlements().isEmpty());
  }

  @Test
  void latestAuthoritativeStatusSupersedesEarlierDelay() {
    when(trips.getTripContext("ABC123")).thenReturn(trip(false, false, false));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(
            new OperationalEvents(
                List.of(
                    delay(180, NOW.minusSeconds(900)),
                    new OperationalEvent(
                        "E2",
                        "S1",
                        "STATUS",
                        null,
                        null,
                        null,
                        "ON_TIME",
                        "OPS_CORE",
                        NOW.minusSeconds(60))),
                meta("COMPLETE")));
    assertEquals(AssessmentStatus.NO_VERIFIED_DISRUPTION, useCase.assess("ABC123", false).status());
    verifyNoInteractions(policies);
  }

  @Test
  void sourceFailureReturnsSafeReview() {
    when(trips.getTripContext("ABC123")).thenThrow(new SourceUnavailableException());
    var result = useCase.assess("ABC123", false);
    assertEquals(List.of("SOURCE_UNAVAILABLE"), result.reviewReasons());
    assertTrue(result.entitlements().isEmpty());
  }

  @Test
  void unknownBookingIsNotEvidenceOfAnUnaffectedTrip() {
    var result = useCase.assess("XYZ999", false);
    assertEquals(AssessmentStatus.REVIEW_REQUIRED, result.status());
    verifyNoInteractions(events, policies);
  }

  @Test
  void invalidIdentifierDoesNotReachSources() {
    assertThrows(
        IllegalArgumentException.class, () -> useCase.assess("private@example.com", false));
    verifyNoInteractions(trips, events, policies);
  }

  @Test
  void futureEventCannotTriggerBenefits() {
    when(trips.getTripContext("ABC123")).thenReturn(trip(false, false, false));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(
            new OperationalEvents(List.of(delay(300, NOW.plusSeconds(1))), meta("COMPLETE")));
    assertTrue(useCase.assess("ABC123", false).entitlements().isEmpty());
    verifyNoInteractions(policies);
  }

  @Test
  void futurePolicyCannotAuthorizeBenefits() {
    var reasons = new ArrayList<String>();
    var policy = policy();
    var invalid =
        new PolicyRules(
            policy.scenario(),
            policy.version(),
            LocalDate.parse("2027-01-01"),
            policy.rules(),
            policy._meta());
    assertTrue(new PolicyEvaluator().evaluate(invalid, 300, true, true, NOW, reasons).isEmpty());
    assertEquals(List.of("POLICY_UNVERIFIED"), reasons);
  }

  @Test
  void unknownRuleIsNotExecutedAsAnExpression() {
    var reasons = new ArrayList<String>();
    var invalid =
        new PolicyRules(
            "DELAY",
            "V1",
            LocalDate.parse("2026-01-01"),
            List.of(new PolicyRule("R9", "always true", "REBOOK_NO_FEE", false)),
            meta("COMPLETE"));
    assertTrue(new PolicyEvaluator().evaluate(invalid, 300, false, false, NOW, reasons).isEmpty());
    assertTrue(reasons.contains("UNKNOWN_POLICY_RULE"));
  }

  @Test
  void missingEventSourceRequiresReviewInsteadOfCrashing() {
    when(trips.getTripContext("ABC123")).thenReturn(trip(false, false, false));
    var event =
        new OperationalEvent(
            "E1", "S1", "DELAY", LocalTime.NOON, 300, null, null, null, NOW.minusSeconds(60));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(new OperationalEvents(List.of(event), meta("COMPLETE")));
    var result = useCase.assess("ABC123", false);
    assertEquals(AssessmentStatus.REVIEW_REQUIRED, result.status());
    assertTrue(result.entitlements().isEmpty());
    verifyNoInteractions(policies);
  }

  @Test
  void emptyPolicyIsNotVerified() {
    var reasons = new ArrayList<String>();
    var invalid =
        new PolicyRules("DELAY", "V1", LocalDate.parse("2026-01-01"), List.of(), meta("COMPLETE"));
    assertTrue(new PolicyEvaluator().evaluate(invalid, 300, false, false, NOW, reasons).isEmpty());
    assertEquals(List.of("POLICY_UNVERIFIED"), reasons);
  }

  @Test
  void sourceFailureAtPolicyRetainsVerifiedDelay() {
    when(trips.getTripContext("ABC123")).thenReturn(trip(false, false, false));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(
            new OperationalEvents(List.of(delay(180, NOW.minusSeconds(60))), meta("COMPLETE")));
    when(policies.getPolicyRules("DELAY")).thenThrow(new SourceUnavailableException());
    var result = useCase.assess("ABC123", false);
    assertEquals(180, result.disruptions().getFirst().delayMinutes());
    assertTrue(result.entitlements().isEmpty());
    assertTrue(result.reviewReasons().contains("SOURCE_UNAVAILABLE"));
  }

  @Test
  void compensationWithoutVerifiedDelayCannotApplyDelayPolicy() {
    when(trips.getTripContext("ABC123")).thenReturn(trip(false, false, false));
    var event =
        new OperationalEvent(
            "E1", "S1", "STATUS", null, null, null, "SCHEDULED", "OPS_CORE", NOW.minusSeconds(60));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(new OperationalEvents(List.of(event), meta("COMPLETE")));
    var result = useCase.assess("ABC123", true);
    assertTrue(result.reviewReasons().contains("COMPENSATION_SCENARIO_UNVERIFIED"));
    assertTrue(result.entitlements().isEmpty());
    verifyNoInteractions(policies);
  }

  @Test
  void freshnessBoundaryIsInclusive() {
    when(trips.getTripContext("ABC123")).thenReturn(trip(false, false, false));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(
            new OperationalEvents(List.of(delay(180, NOW.minusSeconds(1800))), meta("COMPLETE")));
    when(policies.getPolicyRules("DELAY")).thenReturn(policy());
    assertEquals("REBOOK_NO_FEE", useCase.assess("ABC123", false).entitlements().getFirst().code());
  }

  @Test
  void historicalEventsAreRejectedWithLiveClock() {
    var live =
        new AssessDisruptionUseCase(
            trips,
            events,
            policies,
            Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC),
            Duration.ofMinutes(30),
            new PolicyEvaluator());
    when(trips.getTripContext("ABC123")).thenReturn(trip(false, false, false));
    when(events.getOperationalEvents(anyList()))
        .thenReturn(
            new OperationalEvents(List.of(delay(180, NOW.minusSeconds(60))), meta("COMPLETE")));
    var result = live.assess("ABC123", false);
    assertTrue(result.reviewReasons().contains("EVENTS_UNAVAILABLE_OR_STALE"));
    assertTrue(result.disruptions().isEmpty());
    verifyNoInteractions(policies);
  }

  @Test
  void supersededPolicyIsRejected() {
    var reasons = new ArrayList<String>();
    var source = policy();
    var invalid =
        new PolicyRules("DELAY", "OLD", source.effectiveFrom(), source.rules(), meta("COMPLETE"));
    assertTrue(new PolicyEvaluator().evaluate(invalid, 300, false, false, NOW, reasons).isEmpty());
    assertEquals(List.of("POLICY_UNVERIFIED"), reasons);
  }
}
