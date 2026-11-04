package com.disruptioncompanion.infrastructure.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Configuration
@Profile("demo")
public class DemoOpenApiConfiguration {
  private static final String DEMO_PATH = "/api/demo/assessments";
  private static final String ASSESSMENT_PATH = "/api/assessments";
  private static final Set<String> STANDARD_EXAMPLES =
      Set.of("BASE_CASE", "COMPENSATION_REQUESTED", "UNKNOWN_BOOKING", "INVALID_BOOKING");

  @Bean
  OpenApiCustomizer assessmentExamples(ObjectMapper mapper) {
    return openApi -> {
      List<ApiExample> examples =
          Arrays.asList(read(mapper, "api-examples/catalog.json", ApiExample[].class));
      var demo = openApi.getPaths().get(DEMO_PATH);
      if (demo != null) {
        addExamples(demo.getPost(), examples, ApiExample::request, DEMO_PATH, mapper);
      }
      var standard = openApi.getPaths().get(ASSESSMENT_PATH);
      if (standard != null) {
        addExamples(
            standard.getPost(),
            examples.stream()
                .filter(example -> STANDARD_EXAMPLES.contains(example.name()))
                .toList(),
            example -> example.request().get("request"),
            ASSESSMENT_PATH,
            mapper);
      }
    };
  }

  private void addExamples(
      Operation operation,
      List<ApiExample> examples,
      Function<ApiExample, Object> requestValue,
      String path,
      ObjectMapper mapper) {
    Map<String, Example> requests =
        examples.stream()
            .collect(
                Collectors.toMap(
                    ApiExample::name,
                    example ->
                        new Example()
                            .summary(example.description())
                            .value(requestValue.apply(example)),
                    (first, second) -> first,
                    LinkedHashMap::new));
    operation.getRequestBody().getContent().values().forEach(media -> media.setExamples(requests));
    examples.stream()
        .mapToInt(ApiExample::httpStatus)
        .distinct()
        .forEach(
            status -> {
              Map<String, Example> responses =
                  examples.stream()
                      .filter(example -> example.httpStatus() == status)
                      .collect(
                          Collectors.toMap(
                              ApiExample::name,
                              example ->
                                  new Example()
                                      .summary(example.description())
                                      .value(response(mapper, example.responseResource(), path)),
                              (first, second) -> first,
                              LinkedHashMap::new));
              operation
                  .getResponses()
                  .get(Integer.toString(status))
                  .getContent()
                  .values()
                  .forEach(media -> media.setExamples(responses));
            });
    var failure =
        new Example()
            .summary("Unexpected internal failure, not selectable through business inputs")
            .value(response(mapper, "api-examples/responses/unexpected-failure.json", path));
    operation
        .getResponses()
        .get("500")
        .getContent()
        .values()
        .forEach(media -> media.addExamples("UNEXPECTED_FAILURE", failure));
  }

  private Map<String, Object> response(ObjectMapper mapper, String resource, String path) {
    Map<String, Object> response =
        new LinkedHashMap<>(
            mapper.convertValue(
                read(mapper, resource, JsonNode.class),
                new TypeReference<Map<String, Object>>() {}));
    if (response.containsKey("instance")) {
      response.put("instance", path);
    }
    return response;
  }

  private <T> T read(ObjectMapper mapper, String resource, Class<T> type) {
    try (var input = new ClassPathResource(resource).getInputStream()) {
      return mapper.readValue(input, type);
    } catch (IOException exception) {
      throw new IllegalStateException("API example resource could not be loaded", exception);
    }
  }

  private record ApiExample(
      String name,
      String description,
      Map<String, Object> request,
      int httpStatus,
      String responseResource) {}
}
