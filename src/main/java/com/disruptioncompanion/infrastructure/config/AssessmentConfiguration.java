package com.disruptioncompanion.infrastructure.config;

import com.disruptioncompanion.application.port.in.AssessDisruptionUseCase;
import com.disruptioncompanion.application.service.OperationalEventAssessor;
import com.disruptioncompanion.application.service.PolicyEvaluator;
import com.disruptioncompanion.application.service.SourceFreshness;
import com.disruptioncompanion.application.usecase.AssessDisruptionUseCaseImpl;
import com.disruptioncompanion.infrastructure.adapter.out.mock.JsonMockAdapter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;

@Configuration
public class AssessmentConfiguration {
  @Bean
  JsonMockAdapter mockAdapter() {
    ObjectMapper mapper =
        tools.jackson.databind.json.JsonMapper.builder()
            .propertyNamingStrategy(
                new PropertyNamingStrategies.SnakeCaseStrategy() {
                  @Override
                  public String translate(String input) {
                    return "_meta".equals(input) ? "_meta" : super.translate(input);
                  }
                })
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    return new JsonMockAdapter(mapper);
  }

  @Bean
  Clock assessmentClock(@Value("${assessment.reference-instant:}") String reference) {
    return reference.isBlank()
        ? Clock.systemUTC()
        : Clock.fixed(Instant.parse(reference), ZoneOffset.UTC);
  }

  @Bean
  AssessDisruptionUseCase assessDisruption(
      JsonMockAdapter adapter,
      Clock clock,
      @Value("${assessment.max-source-age:PT30M}") Duration maxAge) {
    if (maxAge.isNegative() || maxAge.isZero())
      throw new IllegalArgumentException("Source age must be positive");
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
