package org.humancellatlas.ingest.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
        List<Map<String, Object>> projects = Stream.of("A", "B")
                .map(s -> Project.builder()
                        .withManagedAccess()
                        .withShortName("dataset " + s + " managed")
                        .withUuid(makeUuid(s))
                        .asMap())
                .collect(Collectors.toList());
        // dataset C - open access
        projects.add(Project.builder()
                .withOpenAccess()
                .withShortName("dataset C open")
                .withUuid(makeUuid("C"))
                .asMap());

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
            username = "alice",
            roles = {"CONTRIBUTOR", "access_aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"})
    public void userOnProjectAListCanSeeProjectFilesFiles() throws Exception {
        String projectFilesUrl = projectRepository.findByUuid(new Uuid("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .findFirst()
                .map(Project::getId)
                .map(projectId -> "/projects/" + projectId + "/files")
                .get();

        webApp.perform(get(projectFilesUrl))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(
            username = "bob",
            roles = {"CONTRIBUTOR", "access_bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"})
    public void userNotOnProjectAListCannotSeeProjectFiles() throws Exception {
        String projectFilesUrl = projectRepository.findByUuid(new Uuid("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .findFirst()
                .map(Project::getId)
                .map(projectId -> "/projects/" + projectId + "/files")
                .get();

        webApp.perform(get(projectFilesUrl))
                .andExpect(status().isForbidden());
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

