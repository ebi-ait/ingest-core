package org.humancellatlas.ingest.schemas;

import org.humancellatlas.ingest.schemas.schemascraper.SchemaScraper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
public class SchemaServiceTest {

    @Autowired
    private SchemaService schemaService;

    @MockBean
    private SchemaRepository schemaRepository;

    @MockBean
    private SchemaScraper schemaScraper;

    @Test
    public void testGetLatestSchemas() {
        //given:
        Schema version1_2_3 = createTestSchema("1.2.3");
        Schema version1_2_4 = createTestSchema("1.2.4");
        Schema version2_0 = createTestSchema("2.0");
        Schema version11_1_1 = createTestSchema("11.1.1");

        //and:
        List<Schema> schemas = asList(version2_0, version1_2_3, version11_1_1, version1_2_4);
        doReturn(schemas).when(schemaRepository).findAll();

        //when:
        List<Schema> latestSchemas = schemaService.getLatestSchemas();

        //then:
        assertThat(latestSchemas).hasSize(1);
        Schema latestSchema = latestSchemas.get(0);
        assertThat(latestSchema.getSchemaVersion()).isEqualTo(version11_1_1.getSchemaVersion());
    }

    private Schema createTestSchema(String schemaVersion) {
        return new Schema("core", schemaVersion, "process", "", "process_core",
                "http://schema.humancellatlas.org");
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        SchemaService schemaService() {
            return new SchemaService();
        }

    }

}
