package org.humancellatlas.ingest.project;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@JsonTest
class MetadataDocumentSerializationTest {

  @Autowired private ObjectMapper objectMapper;

  @MockBean
  // NOTE: Adding MigrationConfiguration as a MockBean is needed
  // as otherwise MigrationConfiguration won't be initialised.
  private MigrationConfiguration migrationConfiguration;

  @BeforeEach
  public void setup() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(DataAccessTypes.class, new DataAccessTypesJsonSerializer());
    module.addDeserializer(DataAccessTypes.class, new DataAccessTypesJsonDeserializer());
    objectMapper.registerModule(module);
  }

  @Ignore
  // TODO: this test is not working because it needs proper json input.
  // @ParameterizedTest()
  @MethodSource("testData")
  public void testSerialization(Object someObject, String expectedJson)
      throws JsonProcessingException {
    String json = objectMapper.writeValueAsString(someObject);
    Assertions.assertThat(json).isEqualTo(expectedJson);
  }

  @ParameterizedTest()
  @MethodSource("testData")
  public void testDeserialization(Supplier<Object> objectSupplier, String expectedJson)
      throws JsonProcessingException {
    Object someObject = objectSupplier.get();
    Object deserialized = objectMapper.readValue(expectedJson, someObject.getClass());
    Comparator<Instant> upToSeconds = Comparator.comparing(i -> i.truncatedTo(ChronoUnit.SECONDS));
    Assertions.assertThat(deserialized)
        .usingComparatorForFields(upToSeconds, "contentLastModified")
        .isEqualToComparingFieldByField(someObject);
  }

  @ParameterizedTest()
  @MethodSource("testData")
  public void testSerializationThenDeserialization(
      Supplier<Object> objectSupplier, String expectedJson) throws JsonProcessingException {
    Object someObject = objectSupplier.get();
    String json = objectMapper.writeValueAsString(someObject);
    Object deserialized = objectMapper.readValue(json, someObject.getClass());
    Assertions.assertThat(deserialized).isEqualTo(someObject);
  }

  private static Stream<Arguments> testData() {
    // The test objects are created during test execution so that the time fields will be close
    // to the desrialization target objects. If it were not so, the timestamps would be seconds
    // apart.
    return Stream.of(
        Arguments.of((Supplier) () -> DataAccessTypes.OPEN, "\"All fully open\""),
        Arguments.of(
            (Supplier) () -> new DataAccess(DataAccessTypes.OPEN),
            "{\"type\":\"All fully open\",\"notes\":null}"),
        Arguments.of(
            (Supplier)
                () -> {
                  Project project = new Project(new HashMap<>());
                  // TODO: use project builder
                  ((Map<String, Object>) project.getContent())
                      .put(
                          "dataAccess",
                          new ObjectToMapConverter().asMap(new DataAccess(DataAccessTypes.OPEN)));
                  return project;
                },
            "{\"content\":{\"dataAccess\":{\"type\":\"All fully open\",\"notes\":null}}}"));
  }
}
