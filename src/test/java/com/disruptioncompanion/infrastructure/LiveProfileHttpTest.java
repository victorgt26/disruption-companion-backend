package com.disruptioncompanion.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DisruptionCompanionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("live")
class LiveProfileHttpTest {
    @Autowired
    MockMvc mvc;

    @Test
    void demoEndpointAndFixtureExamplesAreNotExposedOutsideDemo() throws Exception {
        mvc.perform(post("/api/demo/assessments").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/demo/assessments']").doesNotExist())
                .andExpect(
                        jsonPath(
                                "$.paths['/api/assessments'].post.requestBody.content['application/json'].examples")
                                .doesNotExist());
    }
}
