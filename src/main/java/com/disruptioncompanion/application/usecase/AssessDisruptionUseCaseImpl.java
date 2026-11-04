package com.disruptioncompanion.application.usecase;

import com.disruptioncompanion.application.port.in.AssessDisruptionUseCase;
import com.disruptioncompanion.application.port.out.OperationalEventsPort;
import com.disruptioncompanion.application.port.out.PolicyRulesPort;
import com.disruptioncompanion.application.port.out.SourceUnavailableException;
import com.disruptioncompanion.application.port.out.TripContextPort;
import com.disruptioncompanion.application.service.OperationalEventAssessor;
import com.disruptioncompanion.application.service.PolicyEvaluator;
import com.disruptioncompanion.application.service.SourceFreshness;
import com.disruptioncompanion.domain.enums.AssessmentMessage;
import com.disruptioncompanion.domain.enums.AssessmentStatus;
import com.disruptioncompanion.domain.enums.DisruptionScenario;
import com.disruptioncompanion.domain.enums.NextStep;
import com.disruptioncompanion.domain.enums.ReviewReason;
import com.disruptioncompanion.domain.model.Assessment;
import com.disruptioncompanion.domain.model.Entitlement;
import com.disruptioncompanion.domain.model.Metadata;
import com.disruptioncompanion.domain.model.OperationalAssessment;
import com.disruptioncompanion.domain.model.PolicyEvaluation;
import com.disruptioncompanion.domain.model.Segment;
import com.disruptioncompanion.domain.model.TripContext;
import com.disruptioncompanion.domain.model.VerifiedDisruption;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AssessDisruptionUseCaseImpl implements AssessDisruptionUseCase {
  private final TripContextPort trips;
  private final OperationalEventsPort events;
  private final PolicyRulesPort policies;
  private final Clock clock;
  private final SourceFreshness freshness;
  private final OperationalEventAssessor eventAssessor;
  private final PolicyEvaluator evaluator;

  @Override
  public Assessment execute(String bookingId, boolean compensationRequested) {
    validateBookingId(bookingId);
    Instant now = clock.instant();
    List<ReviewReason> reasons = new ArrayList<>();
    OperationalAssessment operational = new OperationalAssessment(List.of(), List.of(), List.of());
    List<Entitlement> entitlements = List.of();
    try {
      TripContext trip = trips.getTripContext(bookingId);
      if (trip == null) {
        return buildAssessment(
            now, operational, entitlements, List.of(ReviewReason.TRIP_NOT_AVAILABLE));
      }
      List<String> segmentIds = segmentIds(trip);
      if (!bookingId.equals(trip.bookingId()) || !hasValidSegments(trip, segmentIds)) {
        return buildAssessment(
            now, operational, entitlements, List.of(ReviewReason.TRIP_INCOMPLETE));
      }
      reasons.addAll(tripReviewReasons(trip, now));
      operational = eventAssessor.assess(segmentIds, events.getOperationalEvents(segmentIds), now);
      reasons.addAll(operational.reviewReasons());
      if (!operational.disruptions().isEmpty()) {
        if (segmentIds.size() > 1) {
          reasons.add(ReviewReason.CONNECTION_OUTCOME_UNKNOWN);
        }
        int maximumDelay =
            operational.disruptions().stream()
                .mapToInt(VerifiedDisruption::delayMinutes)
                .max()
                .orElseThrow();
        PolicyEvaluation policy =
            evaluator.evaluate(
                policies.getPolicyRules(DisruptionScenario.DELAY),
                maximumDelay,
                includesMinor(trip),
                compensationRequested,
                now);
        entitlements = policy.entitlements();
        reasons.addAll(policy.reviewReasons());
      } else if (compensationRequested) {
        reasons.add(ReviewReason.COMPENSATION_SCENARIO_UNVERIFIED);
      }
    } catch (SourceUnavailableException exception) {
      reasons.add(ReviewReason.SOURCE_UNAVAILABLE);
    }
    return buildAssessment(now, operational, entitlements, reasons);
  }

  private void validateBookingId(String bookingId) {
    if (bookingId == null || !bookingId.matches("[A-Z0-9]{6}")) {
      throw new IllegalArgumentException("Invalid booking identifier");
    }
  }

  private List<String> segmentIds(TripContext trip) {
    if (trip.segments() == null) {
      return List.of();
    }
    return trip.segments().stream()
        .filter(Objects::nonNull)
        .map(Segment::segmentId)
        .filter(Objects::nonNull)
        .toList();
  }

  private boolean hasValidSegments(TripContext trip, List<String> segmentIds) {
    return trip.segments() != null
        && !segmentIds.isEmpty()
        && segmentIds.size() == trip.segments().size()
        && segmentIds.stream().noneMatch(String::isBlank)
        && segmentIds.stream().distinct().count() == segmentIds.size();
  }

  private List<ReviewReason> tripReviewReasons(TripContext trip, Instant now) {
    List<ReviewReason> reasons = new ArrayList<>();
    Metadata metadata = trip._meta();
    if (metadata == null || !freshness.isFresh(metadata.retrievedAt(), now)) {
      reasons.add(ReviewReason.TRIP_STALE_OR_UNVERIFIED);
    }
    if (metadata == null
        || !"COMPLETE".equals(metadata.completeness())
        || metadata.missingFields() != null && !metadata.missingFields().isEmpty()) {
      reasons.add(ReviewReason.TRIP_PARTIAL);
    }
    if (trip.passengers() == null
        || trip.passengers().isEmpty()
        || trip.passengers().stream()
            .anyMatch(
                passenger ->
                    passenger == null || passenger.type() == null && passenger.age() == null)) {
      reasons.add(ReviewReason.PASSENGERS_UNKNOWN);
    }
    return List.copyOf(reasons);
  }

  private boolean includesMinor(TripContext trip) {
    return trip.passengers() != null
        && trip.passengers().stream()
            .filter(Objects::nonNull)
            .anyMatch(
                passenger ->
                    "CHILD".equals(passenger.type())
                        || passenger.age() != null && passenger.age() < 18);
  }

  private Assessment buildAssessment(
      Instant now,
      OperationalAssessment operational,
      List<Entitlement> entitlements,
      List<ReviewReason> reasons) {
    boolean disrupted = !operational.disruptions().isEmpty();
    boolean reviewRequired = !reasons.isEmpty();
    AssessmentStatus status =
        reviewRequired
            ? AssessmentStatus.REVIEW_REQUIRED
            : disrupted ? AssessmentStatus.DISRUPTION : AssessmentStatus.NO_VERIFIED_DISRUPTION;
    return new Assessment(
        status,
        now,
        disrupted ? AssessmentMessage.VERIFIED_DELAY : AssessmentMessage.NO_CONFIRMED_CHANGE,
        operational.disruptions(),
        operational.evidence(),
        entitlements,
        reasons.stream().distinct().toList(),
        reviewRequired ? NextStep.CONTACT_HUMAN_SUPPORT : NextStep.READ_VERIFIED_INFORMATION);
  }
}
