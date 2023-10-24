package org.humancellatlas.ingest.security;

import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.*;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.project.BuilderHelper;
import org.humancellatlas.ingest.project.DataAccessTypes;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.humancellatlas.ingest.security.TestDataHelper.makeUuid;
import static org.humancellatlas.ingest.security.TestDataHelper.mapAsJsonString;
import static org.junit.jupiter.api.Assertions.fail;
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
    FileRepository fileRepository;
    @Autowired
    SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @MockBean
    // NOTE: Adding MigrationConfiguration as a MockBean is needed
    // as otherwise MigrationConfiguration won't be initialised.
    private MigrationConfiguration migrationConfiguration;

    @BeforeEach
    @WithMockUser(roles = "WRANGLER")
    public void setupTestData() throws Exception {

        // dataset A - managed access
        List<Map<String, Object>> projects = TestDataHelper.createManagedAccessProjects();
        // dataset C - open access
        projects.add(TestDataHelper.createOpenAccessProjects());

        projects.stream()
                .map(TestDataHelper::mapAsJsonString)
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

        Stream.of("a", "b", "c")
                .map(TestDataHelper::makeUuid)
                .forEach(uuidString -> addMetadataToProjectByProjectUuid(uuidString, File.class));
    }

    @AfterEach
    @WithMockUser(roles = "WRANGLER")
    public void tearDown() {
        Stream.builder()
                        .add(projectRepository)
                        .add(fileRepository)
                        .add(submissionEnvelopeRepository)
                                .build()
                                        .forEach(r->((CrudRepository)r).deleteAll());
        projectRepository.deleteAll();
    }

    @Test
    @WithMockUser(
            username = "alice",
            roles = {"CONTRIBUTOR", "access_aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"})
    public void testDataAccessTypeFieldDeserialization() {
        projectRepository.findByUuid(new Uuid(makeUuid("a")))
                .forEach(p -> Assertions.assertThat(p.getDataAccess())
                        .isEqualTo(DataAccessTypes.MANAGED));
    }


    /**
     * checks access to sub resources of a project, `/projects/{id}/{metadata-type}`
     */
    @Nested
    class MetadataFromProjectAccessControl {

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

    private void addMetadataToProjectByProjectUuid(String uuidString,
                                                   Class<? extends MetadataDocument> metadataType) {
        Project project = projectRepository.findByUuid(new Uuid(uuidString))
                .findFirst().get();
        String submissionUrl = null;
        try {
            submissionUrl = createSubmissionAndGetUrl();
            linkSubmissionToProject(project, submissionUrl);
            MetadataDocument metadataDocument = metadataType.getConstructor().newInstance();
            metadataDocument.setContent(metadataDocument.getType() + " 01 in project " + uuidString);
            String submissionFilesUrl = String.format("%s/%ss", submissionUrl, metadataDocument.getType());
            webApp.perform(post(submissionFilesUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapAsJsonString(BuilderHelper.asMap(metadataDocument, List.of("contentLastModified")))))
                    .andExpect(status().isAccepted());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private ResultActions linkSubmissionToProject(Project project, String submissionUrl) throws Exception {
        return webApp.perform(post("/projects/{id}/submissionEnvelopes", project.getId())
                        .contentType("text/uri-list")
                        .content(submissionUrl))
                .andExpect(status().isNoContent());
    }

    @Nullable
    private String createSubmissionAndGetUrl() throws Exception {
        return webApp.perform(post("/submissionEnvelopes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
    }

    @Nested
    class MetadataRepositoryAccessControl {

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        @WithMockUser(
                username = "alice",
                roles = {"CONTRIBUTOR", "access_aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"})
        public void userOnProjectAList_CanSeeOnlyOpenAndProjectAMetadata(String metadataTypePlural) throws Exception {
            String metadataCollectionUrl = "/" + metadataTypePlural;
            webApp.perform(get(metadataCollectionUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements").value("2"));
        }
    }
}

