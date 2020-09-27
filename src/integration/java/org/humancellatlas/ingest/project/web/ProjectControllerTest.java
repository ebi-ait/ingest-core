package org.humancellatlas.ingest.project.web;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc webApp;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private SchemaService schemaService;

    @Nested
    class PartialUpdate {

        @Test
        void midStepSuccess() throws Exception {
            //given:
            MvcResult result = webApp
                    .perform(patch("/projects?step={step}&totalSteps={steps}", 1, 2))
                    .andReturn();

            //expect:
            Assertions.assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
        }

    }

}