package org.humancellatlas.ingest.project.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.data.MapEntry;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectEventHandler;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc webApp;

    @Autowired
    private ProjectRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private ProjectEventHandler projectEventHandler;

    @MockBean
    private MigrationConfiguration migrationConfiguration;

    @MockBean
    private SchemaService schemaService;

    @Nested
    class Update {

        @Test
        void updateSuccess() throws Exception {
            doTestUpdate("/projects/{id}", project -> {
                var projectCaptor = ArgumentCaptor.forClass(Project.class);
                verify(projectEventHandler).editedProjectMetadata(projectCaptor.capture());
                Project handledProject = projectCaptor.getValue();
                assertThat(handledProject.getId()).isEqualTo(project.getId());
            });
        }

        @Test
        void partialUpdateSuccess() throws Exception {
            doTestUpdate("/projects/{id}?partial=true", project -> {
                verify(projectEventHandler, never()).editedProjectMetadata(any());
            });
        }

        private void doTestUpdate(String patchUrl, Consumer<Project> postCondition) throws Exception {
            //given:
            var content = new HashMap<String, Object>();
            content.put("description", "test");
            Project project = new Project(content);
            project = repository.save(project);

            //when:
            content.put("description", "test updated");
            MvcResult result = webApp
                    .perform(patch(patchUrl, project.getId())
                            .contentType(APPLICATION_JSON_VALUE)
                            .content("{\"content\": " + objectMapper.writeValueAsString(content) + "}"))
                    .andReturn();

            //expect:
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.getContentType()).containsPattern("application/.*json.*");

            //and:
            //Using Map here because reading directly to Project converts the entire JSON to Project.content.
            Map<String, Object> updated = objectMapper.readValue(response.getContentAsString(), Map.class);
            assertThat(updated.get("content")).isInstanceOf(Map.class);
            MapEntry<String, String> updatedDescription = entry("description", "test updated");
            assertThat((Map) updated.get("content")).containsOnly(updatedDescription);

            //and:
            project = repository.findById(project.getId()).get();
            assertThat((Map) project.getContent()).containsOnly(updatedDescription);

            //and:
            postCondition.accept(project);
        }

    }

}