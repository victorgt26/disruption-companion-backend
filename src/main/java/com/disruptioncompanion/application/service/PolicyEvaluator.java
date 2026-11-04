package com.disruptioncompanion.application.service;

import com.disruptioncompanion.domain.enums.DisruptionScenario;
import com.disruptioncompanion.domain.enums.EntitlementCode;
import com.disruptioncompanion.domain.enums.ReviewReason;
import com.disruptioncompanion.domain.model.Entitlement;
import com.disruptioncompanion.domain.model.PolicyEvaluation;
import com.disruptioncompanion.domain.model.PolicyRule;
import com.disruptioncompanion.domain.model.PolicyRules;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class PolicyEvaluator {
    public PolicyEvaluation evaluate(
            PolicyRules policy, int delay, boolean minor, boolean compensation, Instant now) {
        if (!isVerified(policy, now)) {
            return new PolicyEvaluation(List.of(), List.of(ReviewReason.POLICY_UNVERIFIED));
        }
        List<PolicyEvaluation> evaluations =
                policy.rules().stream()
                        .map(rule -> evaluateRule(rule, policy.version(), delay, minor, compensation))
                        .toList();
        return new PolicyEvaluation(
                evaluations.stream().flatMap(result -> result.entitlements().stream()).toList(),
                evaluations.stream()
                        .flatMap(result -> result.reviewReasons().stream())
                        .distinct()
                        .toList());
    }

    private PolicyEvaluation evaluateRule(
            PolicyRule rule, String version, int delay, boolean minor, boolean compensation) {
        return identify(rule)
                .map(known -> evaluateKnownRule(rule, known, version, delay, minor, compensation))
                .orElseGet(
                        () -> new PolicyEvaluation(List.of(), List.of(ReviewReason.UNKNOWN_POLICY_RULE)));
    }

    private PolicyEvaluation evaluateKnownRule(
            PolicyRule rule,
            KnownRule known,
            String version,
            int delay,
            boolean minor,
            boolean compensation) {
        RuleMatch result = match(known, delay, minor, compensation);
        if (result == RuleMatch.UNKNOWN && known == KnownRule.INTERNATIONAL_MEAL) {
            return new PolicyEvaluation(List.of(), List.of(ReviewReason.INTERNATIONAL_STATUS_UNKNOWN));
        }
        if (result != RuleMatch.MATCHED) {
            return new PolicyEvaluation(List.of(), List.of());
        }
        boolean approval = rule.requiresHumanApproval() || known.humanApproval;
        Entitlement entitlement = new Entitlement(known.entitlement, version, rule.ruleId(), approval);
        return new PolicyEvaluation(
                List.of(entitlement), approval ? List.of(ReviewReason.HUMAN_APPROVAL_REQUIRED) : List.of());
    }

    private boolean isVerified(PolicyRules policy, Instant now) {
        return policy != null
                && policy.scenario() == DisruptionScenario.DELAY
                && policy.version() != null
                && !policy.version().isBlank()
                && policy.effectiveFrom() != null
                && !policy.effectiveFrom().isAfter(LocalDate.ofInstant(now, ZoneOffset.UTC))
                && policy.rules() != null
                && !policy.rules().isEmpty()
                && policy._meta() != null
                && policy._meta().retrievedAt() != null
                && !policy._meta().retrievedAt().isAfter(now)
                && (policy._meta().supersededVersions() == null
                || !policy._meta().supersededVersions().contains(policy.version()));
    }

    private Optional<KnownRule> identify(PolicyRule rule) {
        if (rule == null || rule.ruleId() == null || rule.ruleId().isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(KnownRule.values())
                .filter(
                        known ->
                                known.condition.equals(rule.condition()) && known.entitlement == rule.entitlement())
                .findFirst();
    }

    private RuleMatch match(KnownRule rule, int delay, boolean minor, boolean compensation) {
        return switch (rule) {
            case DELAY_REBOOKING -> delay >= 180 ? RuleMatch.MATCHED : RuleMatch.NOT_MATCHED;
            case INTERNATIONAL_MEAL -> delay >= 240 ? RuleMatch.UNKNOWN : RuleMatch.NOT_MATCHED;
            case MISSED_CONNECTION -> RuleMatch.UNKNOWN;
            case CASH_COMPENSATION -> compensation ? RuleMatch.MATCHED : RuleMatch.NOT_MATCHED;
            case MINOR_PRIORITY -> minor ? RuleMatch.MATCHED : RuleMatch.NOT_MATCHED;
        };
    }

    private enum RuleMatch {
        MATCHED,
        NOT_MATCHED,
        UNKNOWN
    }

    private enum KnownRule {
        DELAY_REBOOKING("delay_minutes >= 180", EntitlementCode.REBOOK_NO_FEE, false),
        INTERNATIONAL_MEAL(
                "delay_minutes >= 240 AND international", EntitlementCode.MEAL_VOUCHER, false),
        MISSED_CONNECTION("missed_connection == true", EntitlementCode.REBOOK_NO_FEE, false),
        CASH_COMPENSATION("compensation_requested == true", EntitlementCode.CASH_COMPENSATION, true),
        MINOR_PRIORITY("group_includes_minor == true", EntitlementCode.PRIORITY_HANDLING, true);
        private final String condition;
        private final EntitlementCode entitlement;
        private final boolean humanApproval;

        KnownRule(String condition, EntitlementCode entitlement, boolean humanApproval) {
            this.condition = condition;
            this.entitlement = entitlement;
            this.humanApproval = humanApproval;
        }
    }
}
