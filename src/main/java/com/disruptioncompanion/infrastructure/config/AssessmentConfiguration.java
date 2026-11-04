package com.disruptioncompanion.infrastructure.config;

import com.disruptioncompanion.application.port.in.AssessDisruptionUseCase;
import com.disruptioncompanion.infrastructure.adapter.out.mock.MockFixtures;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.cfg.EnumFeature;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

@Configuration
public class AssessmentConfiguration {
    private ObjectMapper mockMapper() {
        return tools.jackson.databind.json.JsonMapper.builder()
                .propertyNamingStrategy(
                        new PropertyNamingStrategies.SnakeCaseStrategy() {
                            @Override
                            public String translate(String input) {
                                return "_meta".equals(input) ? "_meta" : super.translate(input);
                            }
                        })
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                .build();
    }

    @Bean
    Clock assessmentClock(@Value("${assessment.reference-instant:}") String reference) {
        return reference.isBlank()
                ? Clock.systemUTC()
                : Clock.fixed(Instant.parse(reference), ZoneOffset.UTC);
    }

    @Bean
    AssessmentUseCaseFactory assessmentUseCaseFactory(
            Clock clock, @Value("${assessment.max-source-age:PT30M}") Duration maxAge) {
        if (maxAge.isNegative() || maxAge.isZero()) {
            throw new IllegalArgumentException("Source age must be positive");
        }
        return new AssessmentUseCaseFactory(mockMapper(), clock, maxAge);
    }

    @Bean
    AssessDisruptionUseCase assessDisruption(AssessmentUseCaseFactory factory) {
        return factory.create(MockFixtures.standard());
    }
}
