package com.disruptioncompanion.application.port.out;

import com.disruptioncompanion.domain.model.*;

public interface PolicyRulesPort {
  PolicyRules getPolicyRules(String scenario);
}
