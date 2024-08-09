package org.humancellatlas.ingest.project.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectEventHandler;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.humancellatlas.ingest.state.ValidationState;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @AfterEach
    private void tearDown() {
        repository.deleteAll();
    }

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
            var content = Map.of(
                    "description", "test",
                    "attr2", "should be deleted after patch");
            Project originalProject = repository.save(new Project(content));

            //when:

            Map<String, String> patch = Map.of("description", "test updated");
            MvcResult result = webApp
                    .perform(patch(patchUrl, originalProject.getId())
                            .contentType(APPLICATION_JSON_VALUE)
                            .content("{\"content\": " + objectMapper.writeValueAsString(patch) + "}"))
                    .andReturn();

            //expect:
            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.getContentType()).containsPattern("application/.*json.*");

            //and:
            Project updated = objectMapper.readValue(response.getContentAsString(), Project.class);
            assertThat(updated.getContent()).isInstanceOf(Map.class);
            MapEntry<String, String> updatedDescription = entry("description", "test updated");
            assertThat((Map) updated.getContent()).containsOnly(updatedDescription);

            //and:
            repository.findById(originalProject.getId())
                    .ifPresentOrElse(project -> {
                        assertThat((Map) project.getContent()).containsOnly(updatedDescription);
                        postCondition.accept(project);
                    }, () -> Assertions.fail("project {} not found", originalProject.getId()));

            //and:
        }

        @Test
        void onlyUpdateAllowedFields() throws Exception {
            //given:
            var content = new HashMap<String, Object>();
            content.put("description", "test");
            Project project = new Project(content);
            project = repository.save(project);

            //when:
            content.put("description", "test updated");
            MvcResult result = webApp
                    .perform(patch("/projects/{id}", project.getId())
                            .contentType(APPLICATION_JSON_VALUE)
                            .content("{\"content\": " + objectMapper.writeValueAsString(content) + ", \"validationState\": \"METADATA_VALID\"}"))
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
            assertThat(updated.get("validationState")).isEqualTo("DRAFT");

            //and:
            project = repository.findById(project.getId()).get();
            assertThat((Map) project.getContent()).containsOnly(updatedDescription);
            assertThat(project.getValidationState()).isEqualTo(ValidationState.DRAFT);
        }

    }

    @Nested
    @WithMockUser(roles = "WRNAGLER")
    class Filter {
        @BeforeEach
        public void setup(){
            Project project = makeProject();
            repository.save(project);
        }

        @NotNull
        private Project makeProject() {
            var content = new HashMap<String, Object>();
            content.put("description", "test kw1");
            Project project = new Project(content);
            return project;
        }

        @ParameterizedTest(
                name = "[{index}] all values, some null: {arguments}"
        )
        @CsvSource({
                "kw1,null,null,AllKeywords",
                "kw1,null,null,AnyKeyword",
                "kw1,null,null,UnsuppportedSearchType",
                "kw1,null,null,null",
        })
        @WithMockUser
        public void allValuesSetSomeNull(String search, String wrangler, String wranglingState, String searchType) throws Exception {
            //given:
            var content = Map.of(
                    "search", search,
                    "wrangler", wrangler,
                    "wranglingState", wranglingState,
                    "searchType", searchType
            );

            webApp
                    .perform(get("/projects/filter")
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(content)))
                    .andDo(print())
                    .andExpect(handler().handlerType(ProjectController.class))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest(
                name = "[{index}] nulls missing from filter payload: {arguments}"
        )
        @CsvSource({
                "kw1,null,null,AllKeywords",
                "kw1,null,null,AnyKeyword",
                "kw1,null,null,Unsuppported",
                "kw1,null,null,null",
                "kw1,Amnon,null,null",
                "null,null,NEW,null",
                "null,null,Unsupported,null",
                "null,null,null,null",
        })
        @WithMockUser
        public void nullsAreMissingFromPayload(String search, String wrangler, String wranglingState, String searchType) throws Exception {
            var content = new HashMap<String,String>();
            putIfNotNull(content, search, "search");
            putIfNotNull(content, wrangler, "wrangler");
            putIfNotNull(content, wranglingState, "wranglingState");
            putIfNotNull(content, searchType, "searchType");
            webApp
                    .perform(get("/projects/filter")
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(content)))
                    .andDo(print())
                    .andExpect(handler().handlerType(ProjectController.class))
                    .andExpect(status().isOk());

        }

        private void putIfNotNull(HashMap<String, String> content, String value, String key) {
            if(!"null".equals(value)){
                content.put(key, value);
            }
        }
    }

    @Nested
    class GetProjectSubmissionEnvelopesTests {

        private Project project;

        @BeforeEach
        void setUp() {
            project = makeProject();
            repository.save(project);
        }

        @NotNull
        private Project makeProject() {
            var content = new HashMap<String, Object>();
            content.put("description", "test kw1");
            Project project = new Project(content);
            return project;
        }

        @Test
        @WithMockUser(roles = "WRANGLER")
        void testGetProjectSubmissionEnvelopesAsWrangler() throws Exception {
            MvcResult result = webApp
                    .perform(get("/projects/{id}/submissionEnvelopes", project.getId())
                            .contentType(APPLICATION_JSON_VALUE))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();

            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        @Test
        @WithMockUser(roles = "CONTRIBUTOR")
        void testGetProjectSubmissionEnvelopesAsContributor() throws Exception {
            MvcResult result = webApp
                    .perform(get("/projects/{id}/submissionEnvelopes", project.getId())
                            .contentType(APPLICATION_JSON_VALUE))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            MockHttpServletResponse response = result.getResponse();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        public void testGetProjectSubmissionEnvelopesAsAnonymous() throws Exception {
            webApp.perform(get("/projects/1/submissionEnvelopes"))
                    .andExpect(status().isUnauthorized());
        }
    }

}
