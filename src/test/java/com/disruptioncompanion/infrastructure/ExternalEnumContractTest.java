package com.disruptioncompanion.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.disruptioncompanion.domain.enums.AssessmentStatus;
import com.disruptioncompanion.domain.enums.ReviewReason;
import com.disruptioncompanion.infrastructure.adapter.out.mock.MockFixtures;
import com.disruptioncompanion.infrastructure.config.AssessmentUseCaseFactory;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = DisruptionCompanionApplication.class)
@ActiveProfiles("demo")
class ExternalEnumContractTest {
  private static final String TRIP = "mocks/demo/trips/complete-adult.json";
  private static final String EVENTS = "mocks/demo/events/delay-160.json";
  private static final String POLICY = "mocks/policy.json";
  private static final String UNKNOWN = "mocks/unknown-enums/";
  @Autowired AssessmentUseCaseFactory factory;

  static Stream<Arguments> unknownValues() {
    return Stream.of(
        Arguments.of(
            "completeness",
            new MockFixtures(UNKNOWN + "completeness.json", EVENTS, POLICY),
            ReviewReason.TRIP_PARTIAL),
        Arguments.of(
            "passenger type",
            new MockFixtures(UNKNOWN + "passenger.json", EVENTS, POLICY),
            ReviewReason.PASSENGERS_UNKNOWN),
        Arguments.of(
            "event type",
            new MockFixtures(TRIP, UNKNOWN + "type.json", POLICY),
            ReviewReason.NO_AUTHORITATIVE_EVENT),
        Arguments.of(
            "event status",
            new MockFixtures(TRIP, UNKNOWN + "status.json", POLICY),
            ReviewReason.UNSUPPORTED_OPERATIONAL_STATUS),
        Arguments.of(
            "event source",
            new MockFixtures(TRIP, UNKNOWN + "source.json", POLICY),
            ReviewReason.NO_AUTHORITATIVE_EVENT),
        Arguments.of(
            "policy entitlement",
            new MockFixtures(
                TRIP, "mocks/demo/events/delay-180.json", UNKNOWN + "entitlement.json"),
            ReviewReason.UNKNOWN_POLICY_RULE));
  }

  @ParameterizedTest(name = "Unknown {0} requires review")
  @MethodSource("unknownValues")
  void unknownExternalCodesNeverBecomeVerifiedCategories(
      String category, MockFixtures fixtures, ReviewReason expected) {
    var result = factory.create(fixtures).execute("ABC123", false);
    assertEquals(AssessmentStatus.REVIEW_REQUIRED, result.status());
    assertEquals(List.of(expected), result.reviewReasons());
    assertTrue(result.entitlements().isEmpty());
  }
}
