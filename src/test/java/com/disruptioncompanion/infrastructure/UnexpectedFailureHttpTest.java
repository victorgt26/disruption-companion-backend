package com.disruptioncompanion.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.disruptioncompanion.application.port.in.AssessDisruptionUseCase;
import com.disruptioncompanion.infrastructure.adapter.in.rest.AssessmentController;
import com.disruptioncompanion.infrastructure.exception.ApiExceptionHandler;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

class UnexpectedFailureHttpTest {
  @Test
  void unexpectedFailureMatchesPublishedExampleWithoutLeakingDetails() throws Exception {
    var useCase = mock(AssessDisruptionUseCase.class);
    when(useCase.execute("ABC123", false))
        .thenThrow(new IllegalStateException("sensitive internal detail"));
    var mvc =
        MockMvcBuilders.standaloneSetup(new AssessmentController(useCase))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    String actual =
        mvc.perform(
                post("/api/assessments")
                    .contentType("application/json")
                    .content("{\"bookingId\":\"ABC123\"}"))
            .andExpect(status().isInternalServerError())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String expected =
        new ClassPathResource("api-examples/responses/unexpected-failure.json")
            .getContentAsString(StandardCharsets.UTF_8);
    var mapper = JsonMapper.builder().build();
    assertEquals(mapper.readTree(expected), mapper.readTree(actual));
    assertFalse(actual.contains("sensitive"));
  }
}
