package uk.ac.ebi.subs.ingest.security;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.subs.ingest.project.Project;

public class TestDataHelper {
  @NotNull
  static String makeUuid(String s) {
    if (s.length() != 1) {
      throw new IllegalArgumentException("use a single character");
    }
    return s.repeat(8)
        + "-"
        + s.repeat(4)
        + "-"
        + s.repeat(4)
        + "-"
        + s.repeat(4)
        + "-"
        + s.repeat(12);
  }

  static Map<String, Object> createOpenAccessProjects() {
    // TODO amnon: exclusion of contentLastModified needed because of serialization problem. Not
    // sure why.\n"
    return Project.builder()
        .withOpenAccess()
        .withShortName("dataset C open")
        .withUuid(makeUuid("C"))
        .asMap();
  }

  @NotNull
  static List<Map<String, Object>> createManagedAccessProjects() {
    return Stream.of("A", "B")
        .map(
            s ->
                Project.builder()
                    .withManagedAccess()
                    .withShortName("dataset " + s + " managed")
                    .withUuid(makeUuid(s))
                    .asMap())
        .collect(Collectors.toList());
  }

  static String mapAsJsonString(Map<String, Object> value) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
