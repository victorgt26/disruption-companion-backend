package com.disruptioncompanion.application.service;

import com.disruptioncompanion.domain.enums.EvidenceDecision;
import com.disruptioncompanion.domain.enums.OperationalEventType;
import com.disruptioncompanion.domain.enums.OperationalSource;
import com.disruptioncompanion.domain.enums.OperationalStatus;
import com.disruptioncompanion.domain.enums.ReviewReason;
import com.disruptioncompanion.domain.model.Evidence;
import com.disruptioncompanion.domain.model.OperationalAssessment;
import com.disruptioncompanion.domain.model.OperationalEvent;
import com.disruptioncompanion.domain.model.OperationalEvents;
import com.disruptioncompanion.domain.model.VerifiedDisruption;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
public final class OperationalEventAssessor {
    private static final OperationalSource AUTHORITATIVE_SOURCE = OperationalSource.OPS_CORE;
    private static final Set<OperationalEventType> SUPPORTED_EVENT_TYPES = Set.of(OperationalEventType.DELAY, OperationalEventType.STATUS);
    private static final Set<OperationalStatus> UNCHANGED_STATUSES = Set.of(OperationalStatus.ON_TIME, OperationalStatus.SCHEDULED);
    private final SourceFreshness freshness;

    public OperationalAssessment assess(
            List<String> segmentIds, OperationalEvents feed, Instant now) {
        if (!isFeedVerified(feed, now)) {
            return new OperationalAssessment(List.of(), List.of(), List.of(ReviewReason.EVENTS_UNAVAILABLE_OR_STALE));
        }
        List<VerifiedDisruption> disruptions = new ArrayList<>();
        List<Evidence> evidence = new ArrayList<>();
        List<ReviewReason> reasons = new ArrayList<>();
        for (String segmentId : segmentIds) {
            List<OperationalEvent> segmentEvents = feed.events().stream()
                    .filter(Objects::nonNull)
                    .filter(event -> segmentId.equals(event.segmentId()))
                    .toList();
            evidence.addAll(segmentEvents.stream().map(event -> toEvidence(event, feed, now)).toList());
            List<OperationalEvent> authoritativeEvents = segmentEvents.stream()
                    .filter(event -> isVerified(event, feed, now))
                    .filter(this::isAuthoritative)
                    .toList();
            assessSegment(authoritativeEvents, disruptions, reasons);
        }
        return new OperationalAssessment(List.copyOf(disruptions), List.copyOf(evidence), reasons.stream().distinct().toList());
    }

    private boolean isFeedVerified(OperationalEvents feed, Instant now) {
        return feed != null
                && feed.events() != null
                && feed._meta() != null
                && freshness.isFresh(feed._meta().retrievedAt(), now);
    }

    private Evidence toEvidence(OperationalEvent event, OperationalEvents feed, Instant now) {
        EvidenceDecision decision = EvidenceDecision.IGNORED_STALE_DEGRADED_OR_UNVERIFIED;
        if (isVerified(event, feed, now)) {
            decision = isAuthoritative(event)
                    ? EvidenceDecision.AUTHORITATIVE_CANDIDATE
                    : EvidenceDecision.CONTEXT_ONLY;
        }
        return new Evidence(event.eventId(), event.segmentId(), event.source(), event.recordedAt(), decision);
    }

    private boolean isVerified(OperationalEvent event, OperationalEvents feed, Instant now) {
        List<OperationalSource> degraded = feed._meta().sourcesDegraded();
        List<OperationalSource> available = feed._meta().sourcesAvailable();
        return event.eventId() != null
                && !event.eventId().isBlank()
                && event.source() != null
                && freshness.isFresh(event.recordedAt(), now)
                && !event.recordedAt().isAfter(feed._meta().retrievedAt())
                && (degraded == null || !degraded.contains(event.source()))
                && available != null
                && available.contains(event.source());
    }

    private boolean isAuthoritative(OperationalEvent event) {
        return event.source() == AUTHORITATIVE_SOURCE
                && event.type() != null
                && SUPPORTED_EVENT_TYPES.contains(event.type());
    }

    private void assessSegment(
            List<OperationalEvent> events,
            List<VerifiedDisruption> disruptions,
            List<ReviewReason> reasons) {
        if (events.isEmpty()) {
            reasons.add(ReviewReason.NO_AUTHORITATIVE_EVENT);
            return;
        }
        Instant latest = events.stream()
                .map(OperationalEvent::recordedAt)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        List<OperationalEvent> current = events.stream().filter(event -> latest.equals(event.recordedAt())).toList();
        OperationalEvent selected = current.getFirst();
        if (current.stream().anyMatch(event -> !sameOperationalState(selected, event))) {
            reasons.add(ReviewReason.CONFLICTING_AUTHORITATIVE_EVENTS);
            return;
        }
        if (selected.type() == OperationalEventType.DELAY) {
            assessDelay(selected, disruptions, reasons);
        } else if (selected.status() == null || !UNCHANGED_STATUSES.contains(selected.status())) {
            reasons.add(ReviewReason.UNSUPPORTED_OPERATIONAL_STATUS);
        }
    }

    private boolean sameOperationalState(OperationalEvent first, OperationalEvent second) {
        return Objects.equals(first.type(), second.type())
                && Objects.equals(first.status(), second.status())
                && Objects.equals(first.delayMinutes(), second.delayMinutes())
                && Objects.equals(first.newDeparture(), second.newDeparture());
    }

    private void assessDelay(OperationalEvent event, List<VerifiedDisruption> disruptions, List<ReviewReason> reasons) {
        if (event.delayMinutes() == null || event.delayMinutes() < 0 || event.newDeparture() == null) {
            reasons.add(ReviewReason.EVENT_INCOMPLETE);
            return;
        }
        if (event.delayMinutes() > 0) {
            disruptions.add(new VerifiedDisruption(
                    event.segmentId(), event.eventId(), event.delayMinutes(), event.newDeparture()
            ));
        }
    }
}
