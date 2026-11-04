package com.disruptioncompanion.infrastructure;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = DisruptionCompanionApplication.class)
@AutoConfigureMockMvc
class AssessmentHttpTest {
  @Autowired MockMvc mvc;

  @Test
  void baseCaseUsesRealFixturesWithoutLeakingPersonalData() throws Exception {
    mvc.perform(
            post("/api/assessments")
                .contentType("application/json")
                .content("{\"bookingId\":\"ABC123\"}"))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(jsonPath("$.status").value("REVIEW_REQUIRED"))
        .andExpect(jsonPath("$.disruptions[0].delayMinutes").value(160))
        .andExpect(jsonPath("$.entitlements[0].code").value("PRIORITY_HANDLING"))
        .andExpect(jsonPath("$.entitlements[0].policyRef").value("POL-DELAY-2026-03"))
        .andExpect(content().string(not(containsString("ABC123"))))
        .andExpect(content().string(not(containsString("passenger"))))
        .andExpect(content().string(not(containsString("document_id"))));
  }

  @Test
  void invalidRequestDoesNotEchoRejectedValue() throws Exception {
    mvc.perform(
            post("/api/assessments")
                .contentType("application/json")
                .content("{\"bookingId\":\"private@example.com\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(not(containsString("private@example.com"))));
  }

  @Test
  void malformedJsonReturnsGenericProblem() throws Exception {
    mvc.perform(post("/api/assessments").contentType("application/json").content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid request"));
  }

  @Test
  void unknownBookingRequiresReview() throws Exception {
    mvc.perform(
            post("/api/assessments")
                .contentType("application/json")
                .content("{\"bookingId\":\"XYZ999\"}"))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(jsonPath("$.reviewReasons[0]").value("TRIP_NOT_AVAILABLE"));
  }

  @Test
  void explicitCompensationIsSubjectToHumanApproval() throws Exception {
    mvc.perform(
            post("/api/assessments")
                .contentType("application/json")
                .content("{\"bookingId\":\"ABC123\",\"compensationRequested\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.entitlements[0].code").value("CASH_COMPENSATION"))
        .andExpect(jsonPath("$.entitlements[0].requiresHumanApproval").value(true));
  }
}
