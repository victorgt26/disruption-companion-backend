package com.disruptioncompanion.infrastructure.config;

import com.disruptioncompanion.application.port.in.AssessDisruptionUseCase;
import com.disruptioncompanion.application.service.OperationalEventAssessor;
import com.disruptioncompanion.application.service.PolicyEvaluator;
import com.disruptioncompanion.application.service.SourceFreshness;
import com.disruptioncompanion.application.usecase.AssessDisruptionUseCaseImpl;
import com.disruptioncompanion.infrastructure.adapter.out.mock.JsonMockAdapter;
import com.disruptioncompanion.infrastructure.adapter.out.mock.MockFixtures;
import java.time.Clock;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public final class AssessmentUseCaseFactory {
  private final ObjectMapper mapper;
  private final Clock clock;
  private final Duration maxAge;

  public AssessDisruptionUseCase create(MockFixtures fixtures) {
    JsonMockAdapter adapter = new JsonMockAdapter(mapper, fixtures);
    SourceFreshness freshness = new SourceFreshness(maxAge);
    return new AssessDisruptionUseCaseImpl(
        adapter,
        adapter,
        adapter,
        clock,
        freshness,
        new OperationalEventAssessor(freshness),
        new PolicyEvaluator());
  }
}
