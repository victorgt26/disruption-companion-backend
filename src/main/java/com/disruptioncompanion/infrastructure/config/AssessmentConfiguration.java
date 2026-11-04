package com.disruptioncompanion.infrastructure.config;

import com.disruptioncompanion.application.port.in.AssessDisruption;
import com.disruptioncompanion.application.service.PolicyEvaluator;
import com.disruptioncompanion.application.usecase.AssessDisruptionUseCase;
import com.disruptioncompanion.infrastructure.adapter.out.mock.JsonMockAdapter;
import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
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
  AssessDisruption assessDisruption(
      JsonMockAdapter adapter,
      Clock clock,
      @Value("${assessment.max-source-age:PT30M}") Duration maxAge) {
    if (maxAge.isNegative() || maxAge.isZero())
      throw new IllegalArgumentException("Source age must be positive");
    return new AssessDisruptionUseCase(
        adapter, adapter, adapter, clock, maxAge, new PolicyEvaluator());
  }
}
