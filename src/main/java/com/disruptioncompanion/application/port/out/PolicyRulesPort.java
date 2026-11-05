package com.disruptioncompanion.application.port.out;

import com.disruptioncompanion.domain.enums.DisruptionScenario;
import com.disruptioncompanion.domain.model.PolicyRules;

public interface PolicyRulesPort {
    PolicyRules getPolicyRules(DisruptionScenario scenario);
}
