package com.disruptioncompanion.infrastructure;

import com.disruptioncompanion.domain.enums.AssessmentStatus;
import com.disruptioncompanion.domain.enums.ReviewReason;
import com.disruptioncompanion.infrastructure.adapter.in.rest.dto.AssessmentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DisruptionCompanionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("demo")
class DemoAssessmentHttpTest {
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    @Autowired
    MockMvc mvc;

    static Stream<Arguments> examples() throws IOException {
        return catalog().stream().map(example -> Arguments.of(example.name(), example));
    }

    private static List<ApiExample> catalog() throws IOException {
        try (var input = new ClassPathResource("api-examples/catalog.json").getInputStream()) {
            return Arrays.asList(MAPPER.readValue(input, ApiExample[].class));
        }
    }

    private static String resource(String path) throws IOException {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("examples")
    void executesRealUseCaseAndMatchesPublishedResponse(String name, ApiExample example)
            throws Exception {
        var result =
                mvc.perform(
                                post("/api/demo/assessments")
                                        .contentType("application/json")
                                        .content(MAPPER.writeValueAsString(example.request())))
                        .andExpect(status().is(example.httpStatus()))
                        .andReturn();
        assertEquals(
                MAPPER.readTree(resource(example.responseResource())),
                MAPPER.readTree(result.getResponse().getContentAsString()));
    }

    @Test
    void catalogCoversAllCurrentBusinessStatusesAndReviewReasons() throws IOException {
        List<AssessmentResponse> responses =
                catalog().stream()
                        .filter(example -> example.httpStatus() == 200)
                        .map(
                                example ->
                                        MAPPER.readValue(
                                                resourceUnchecked(example.responseResource()), AssessmentResponse.class))
                        .toList();
        assertEquals(
                Set.of(AssessmentStatus.values()),
                responses.stream().map(AssessmentResponse::status).collect(Collectors.toSet()));
        assertEquals(
                Set.of(ReviewReason.values()),
                responses.stream()
                        .flatMap(response -> response.reviewReasons().stream())
                        .collect(Collectors.toSet()));
        assertTrue(
                responses.stream()
                        .flatMap(response -> response.entitlements().stream())
                        .allMatch(entitlement -> entitlement.policyRef() != null));
    }

    private static String resourceUnchecked(String path) {
        try {
            return resource(path);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @Test
    void swaggerIncludesExecutableNamedRequestsAndCapturedResponses() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.paths['/api/demo/assessments'].post.requestBody.content['application/json'].examples.DELAY_AT_THRESHOLD.value.scenario")
                                .value("DELAY_AT_THRESHOLD"))
                .andExpect(
                        jsonPath(
                                "$.paths['/api/assessments'].post.requestBody.content['application/json'].examples.COMPENSATION_REQUESTED.value.compensationRequested")
                                .value(true))
                .andExpect(
                        jsonPath(
                                "$.paths['/api/demo/assessments'].post.responses['200'].content.*.examples.NO_DISRUPTION.value.status")
                                .value(org.hamcrest.Matchers.hasItem("NO_VERIFIED_DISRUPTION")))
                .andExpect(
                        jsonPath(
                                "$.paths['/api/demo/assessments'].post.responses['500'].content.*.examples.UNEXPECTED_FAILURE.value.status")
                                .value(org.hamcrest.Matchers.hasItem(500)));
    }

    private record ApiExample(
            String name,
            String description,
            Map<String, Object> request,
            int httpStatus,
            String responseResource) {
    }
}
