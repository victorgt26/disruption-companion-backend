package com.disruptioncompanion.application.service;

import com.disruptioncompanion.domain.model.*;
import java.time.*;
import java.util.*;

public final class PolicyEvaluator {
  public List<Entitlement> evaluate(
      PolicyRules policy,
      int delay,
      boolean minor,
      boolean compensation,
      Instant now,
      List<String> reasons) {
    if (policy == null
        || !"DELAY".equals(policy.scenario())
        || policy.version() == null
        || policy.version().isBlank()
        || policy.effectiveFrom() == null
        || policy.effectiveFrom().isAfter(LocalDate.ofInstant(now, ZoneOffset.UTC))
        || policy.rules() == null
        || policy.rules().isEmpty()
        || policy._meta() == null
        || policy._meta().retrievedAt() == null
        || policy._meta().retrievedAt().isAfter(now)
        || (policy._meta().supersededVersions() != null
            && policy._meta().supersededVersions().contains(policy.version()))) {
      reasons.add("POLICY_UNVERIFIED");
      return List.of();
    }
    List<Entitlement> result = new ArrayList<>();
    for (PolicyRule rule : policy.rules()) {
      if (rule == null || rule.condition() == null || rule.ruleId() == null) {
        reasons.add("UNKNOWN_POLICY_RULE");
        continue;
      }
      Boolean matched =
          switch (rule.condition()) {
            case "delay_minutes >= 180" -> delay >= 180;
            case "delay_minutes >= 240 AND international" -> delay < 240 ? false : null;
            case "missed_connection == true" -> null;
            case "compensation_requested == true" -> compensation;
            case "group_includes_minor == true" -> minor;
            default -> null;
          };
      String expected =
          switch (rule.condition()) {
            case "delay_minutes >= 180", "missed_connection == true" -> "REBOOK_NO_FEE";
            case "delay_minutes >= 240 AND international" -> "MEAL_VOUCHER";
            case "compensation_requested == true" -> "CASH_COMPENSATION";
            case "group_includes_minor == true" -> "PRIORITY_HANDLING";
            default -> "";
          };
      if (!expected.equals(rule.entitlement())) {
        reasons.add("UNKNOWN_POLICY_RULE");
        continue;
      }
      if (matched == null) {
        if (!Set.of("missed_connection == true", "delay_minutes >= 240 AND international")
            .contains(rule.condition())) reasons.add("UNKNOWN_POLICY_RULE");
        else if (delay >= 240 && rule.condition().contains("international"))
          reasons.add("INTERNATIONAL_STATUS_UNKNOWN");
        continue;
      }
      if (matched) {
        boolean approval =
            rule.requiresHumanApproval()
                || compensation && rule.condition().contains("compensation")
                || rule.condition().contains("minor");
        result.add(new Entitlement(rule.entitlement(), policy.version(), rule.ruleId(), approval));
        if (approval) reasons.add("HUMAN_APPROVAL_REQUIRED");
      }
    }
    return List.copyOf(result);
  }
}
