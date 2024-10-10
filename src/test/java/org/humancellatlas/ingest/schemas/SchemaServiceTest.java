package org.humancellatlas.ingest.schemas;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.humancellatlas.ingest.schemas.schemascraper.SchemaScraper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class SchemaServiceTest {

  @Autowired private SchemaService schemaService;

  @MockBean private SchemaRepository schemaRepository;

  @MockBean private SchemaScraper schemaScraper;

  @Test
  public void testGetLatestSchemasVersions() {
    // given:
    Schema version1_2_3 = createTestSchema("1.2.3", "process_core");
    Schema version1_2_4 = createTestSchema("1.2.4", "process_core");
    Schema version2_0 = createTestSchema("2.0", "process_core");
    Schema version11_1_1 = createTestSchema("11.1.1", "process_core");

    // and:
    List<Schema> schemas = asList(version2_0, version1_2_3, version11_1_1, version1_2_4);
    doReturn(schemas).when(schemaRepository).findAll();

    // when:
    List<Schema> latestSchemas = schemaService.getLatestSchemas();

    // then:
    assertThat(latestSchemas).hasSize(1);
    Schema latestSchema = latestSchemas.get(0);
    assertThat(latestSchema.getSchemaVersion()).isEqualTo(version11_1_1.getSchemaVersion());
  }

  @Test
  public void testGetLatestSchemasCount() {
    // given:
    Schema version1_2_3 = createTestSchema("1.2.3", "process_core");
    Schema version1_2_4 = createTestSchema("1.2.4", "process_core");
    Schema version2_0 = createTestSchema("2.0", "process_core");
    Schema version11_1_1 = createTestSchema("11.1.1", "process_core");
    Schema protocol_version1_2_3 = createTestSchema("1.2.3", "protocol");
    Schema biomaterial_version1_2_4 = createTestSchema("1.2.4", "biomaterial");
    Schema project_version2_0 = createTestSchema("2.0", "project");
    Schema project_version11_1_1 = createTestSchema("11.1.1", "project");

    // and:
    List<Schema> schemas =
        asList(
            version2_0,
            version1_2_3,
            version11_1_1,
            version1_2_4,
            protocol_version1_2_3,
            biomaterial_version1_2_4,
            project_version2_0,
            project_version11_1_1);
    doReturn(schemas).when(schemaRepository).findAll();

    // when:
    List<Schema> latestSchemas = schemaService.getLatestSchemas();

    // then:
    assertThat(latestSchemas).hasSize(4);
  }

  @Test
  public void testGetLatestSchemaByEntityTypeWithExistingType() {
    // given:
    final String projectEntityType = "project";
    final String oldSchemaVersion = "2.0";
    final String newSchemaVersion = "11.1.1";
    Schema protocol_version1_2_3 = createTestSchema(oldSchemaVersion, "protocol");
    Schema biomaterial_version1_2_4 = createTestSchema(newSchemaVersion, "biomaterial");
    Schema project_version2_0 = createTestSchema(oldSchemaVersion, projectEntityType);
    Schema project_version11_1_1 = createTestSchema(newSchemaVersion, projectEntityType);

    // and:
    List<Schema> schemas =
        asList(
            project_version2_0,
            protocol_version1_2_3,
            project_version11_1_1,
            biomaterial_version1_2_4);
    doReturn(schemas).when(schemaRepository).findAll();

    // when:
    Schema latestSchema = schemaService.getLatestSchemaByEntityType("type", projectEntityType);

    // then:
    assertThat(latestSchema.getConcreteEntity()).isEqualTo(projectEntityType);
    assertThat(latestSchema.getSchemaVersion()).isEqualTo(newSchemaVersion);
  }

  @Test
  public void testGetLatestSchemaByEntityTypeWithNonExistingType() {
    // given:
    final String projectEntityType = "project";
    final String oldSchemaVersion = "2.0";
    final String newSchemaVersion = "11.1.1";
    Schema protocol_version1_2_3 = createTestSchema(oldSchemaVersion, "protocol");
    Schema biomaterial_version1_2_4 = createTestSchema(newSchemaVersion, "biomaterial");
    Schema project_version2_0 = createTestSchema(oldSchemaVersion, projectEntityType);
    Schema project_version11_1_1 = createTestSchema(newSchemaVersion, projectEntityType);

    // and:
    List<Schema> schemas =
        asList(
            project_version2_0,
            protocol_version1_2_3,
            project_version11_1_1,
            biomaterial_version1_2_4);
    doReturn(schemas).when(schemaRepository).findAll();

    // when:
    Schema latestSchema = schemaService.getLatestSchemaByEntityType("type", "non_exists");

    // then:
    assertThat(latestSchema).isNull();
  }

  private Schema createTestSchema(String schemaVersion, String entityType) {
    return new Schema(
        "type",
        schemaVersion,
        entityType,
        entityType,
        entityType,
        "http://schema.humancellatlas.org");
  }

  @Configuration
  static class TestConfiguration {

    @Bean
    SchemaService schemaService() {
      return new SchemaService();
    }
  }

  @Test
  public void testGetLatestMorphicSchemasVersions() {
    // given:
    Schema version1_0_0 = createMorphicTestSchema("1.0.0", "biomaterial");
    Schema version0_9_0 = createMorphicTestSchema("0.9.0", "biomaterial");
    Schema version2_0_0 = createMorphicTestSchema("2.0.0", "biomaterial");

    // and:
    List<Schema> schemas = asList(version2_0_0, version0_9_0, version1_0_0);
    doReturn(schemas).when(schemaRepository).findAll();

    // when:
    List<Schema> latestSchemas = schemaService.getLatestSchemas();

    // then:
    assertThat(latestSchemas).hasSize(1);
    Schema latestSchema = latestSchemas.get(0);
    assertThat(latestSchema.getSchemaVersion()).isEqualTo(version2_0_0.getSchemaVersion());
  }

  private Schema createMorphicTestSchema(String schemaVersion, String entityType) {
    return new Schema(
        "type", schemaVersion, entityType, "", entityType, "https://dev.schema.morphic.bio");
  }
}
