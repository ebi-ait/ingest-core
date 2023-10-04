package org.humancellatlas.ingest.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.project.DataAccessTypes;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo()
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class ManagedAccessTest {
    @Autowired
    private MockMvc webApp;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    // NOTE: Adding MigrationConfiguration as a MockBean is needed
    // as otherwise MigrationConfiguration won't be initialised.
    private MigrationConfiguration migrationConfiguration;

    @BeforeEach
    public void setupTestData() throws Exception {

        // dataset A - managed access
        List<Map<String, Object>> projects = createManagedAccessProjects();
        // dataset C - open access
        createOpenAccessProjects(projects);

        projects.stream()
                .map(ManagedAccessTest::mapAsJsonString)
                .forEach(p -> {
                    try {
                        webApp.perform(post("/projects")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(p))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        projectRepository.findByUuid(new Uuid(makeUuid("a")))
                .forEach(p-> Assertions.assertThat(p.getDataAccess())
                        .isEqualTo(DataAccessTypes.MANAGED));
    }

    private static void createOpenAccessProjects(List<Map<String, Object>> projects) {
        projects.add(Project.builder()
                .withOpenAccess()
                .withShortName("dataset C open")
                .withUuid(makeUuid("C"))
                .asMap());
    }

    @NotNull
    private static List<Map<String, Object>> createManagedAccessProjects() {
        List<Map<String, Object>> projects = Stream.of("A", "B")
                .map(s -> Project.builder()
                        .withManagedAccess()
                        .withShortName("dataset " + s + " managed")
                        .withUuid(makeUuid(s))
                        .asMap())
                .collect(Collectors.toList());
        return projects;
    }

    private static String mapAsJsonString(Map<String, Object> value) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static String makeUuid(String s) {
        if (s.length() != 1) {
            throw new IllegalArgumentException("use a single character");
        }
        return s.repeat(8) + "-" + s.repeat(4) + "-" + s.repeat(4) + "-" + s.repeat(4) + "-" + s.repeat(12);
    }

    @AfterEach
    public void tearDown() {
        projectRepository.deleteAll();
    }




    @Nested
    class MetadataAccessControl {

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        @WithMockUser(
                username = "alice",
                roles = {"CONTRIBUTOR", "access_aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"})
        public void userOnProjectAList_CanSeeProjectMetadata(String metadataTypePlural) throws Exception {
            String projectMetadataUrl = projectRepository.findByUuid(new Uuid(makeUuid("a")))
                    .findFirst()
                    .map(Project::getId)
                    .map(projectId -> String.format("/projects/%s/%s", projectId, metadataTypePlural))
                    .get();

            webApp.perform(get(projectMetadataUrl))
                    .andExpect(status().isOk());
        }
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        @WithMockUser(
                username = "alice",
                roles = {"CONTRIBUTOR", "access_aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"})
        public void userOnProjectAList_CanSeeOpenProjectMetadata(String metadataTypePlural) throws Exception {
            String projectMetadataUrl = projectRepository.findByUuid(new Uuid(makeUuid("c")))
                    .findFirst()
                    .map(Project::getId)
                    .map(projectId -> String.format("/projects/%s/%s", projectId, metadataTypePlural))
                    .get();

            webApp.perform(get(projectMetadataUrl).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        @WithMockUser(
                username = "bob",
                roles = {"CONTRIBUTOR", "access_bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"})
        public void userNotOnProjectAList_CannotSeeMetadata(String metadataTypePlural) throws Exception {
            String projectMetadataUrl = projectRepository.findByUuid(new Uuid(makeUuid("a")))
                    .findFirst()
                    .map(Project::getId)
                    .map(projectId -> String.format("/projects/%s/%s", projectId, metadataTypePlural))
                    .get();

            webApp.perform(get(projectMetadataUrl))
                    .andExpect(status().isForbidden());
        }
    }


    @Nested
    class ProjectAccessControl {
        @Test
        @WithMockUser(
                username = "alice",
                roles = {"CONTRIBUTOR", "access_aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"})
        public void userOnProjectAListCanSeeAllProjects() throws Exception {

            webApp.perform(get("/projects"))
                    .andExpect(jsonPath("$.page.totalElements").value("3"));
        }
        @Test
        @WithMockUser(
                username = "bob",
                roles = {"CONTRIBUTOR"})
        public void userNotOnProjectAListCanSeeAllProjects() throws Exception {
            webApp.perform(get("/projects"))
                    .andExpect(jsonPath("$.page.totalElements").value("3"));
        }
    }

}

