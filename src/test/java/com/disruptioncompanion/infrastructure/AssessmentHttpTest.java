package com.disruptioncompanion.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DisruptionCompanionApplication.class)
@AutoConfigureMockMvc
class AssessmentHttpTest {
    @Autowired
    MockMvc mvc;

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

    @Test
    void openApiDocumentsOperationExampleAndEnumValues() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/assessments'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/assessments'].post.responses['400']").exists())
                .andExpect(
                        jsonPath("$.components.schemas.AssessmentRequest.properties.bookingId.example")
                                .value("ABC123"))
                .andExpect(
                        jsonPath("$.components.schemas.AssessmentResponse.properties.nextStep.enum")
                                .value(hasItems("CONTACT_HUMAN_SUPPORT", "READ_VERIFIED_INFORMATION")));
    }

    @Test
    void swaggerUiServesItsLocalWebConsole() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }
}
