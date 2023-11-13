package org.humancellatlas.ingest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.AbstractEntity;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.*;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.humancellatlas.ingest.TestingHelper.resetTestingSecurityContext;
import static org.humancellatlas.ingest.security.TestDataHelper.makeUuid;
import static org.humancellatlas.ingest.security.TestDataHelper.mapAsJsonString;
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
    BiomaterialRepository biomaterialRepository;

    @Autowired
    ProtocolRepository protocolRepository;

    @Autowired
    ProcessRepository processRepository;

    @Autowired
    SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    // NOTE: Adding MigrationConfiguration as a MockBean is needed
    // as otherwise MigrationConfiguration won't be initialised.
    private MigrationConfiguration migrationConfiguration;

    @BeforeEach
    @WithMockUser(roles = "WRANGLER")
    public void setupTestData() throws Exception {

        // datasets A, B - managed access
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
                        resetTestingSecurityContext();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Stream.of("a", "b", "c")
                .map(TestDataHelper::makeUuid)
                .forEach(uuidString -> addMetadataToProjectByProjectUuid(uuidString,
                        List.of(File.class, Biomaterial.class, Protocol.class, Process.class)));
    }

    @AfterEach
    @WithMockUser(roles = "WRANGLER")
    public void tearDown() {
        Stream.builder()
                        .add(projectRepository)
                        .add(fileRepository)
                        .add(biomaterialRepository)
                        .add(protocolRepository)
                        .add(processRepository)
                        .add(submissionEnvelopeRepository)
                                .build()
                                        .forEach(r->((CrudRepository)r).deleteAll());
    }

    @Test
    @WithMockUser(
            username = "alice",
            roles = {"CONTRIBUTOR", "access_aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"})
    public void testDataAccessTypeFieldDeserialization() {
        projectRepository.findByUuid(new Uuid(makeUuid("a")))
                .forEach(p -> Assertions.assertThat(p.getContent())
                        .extracting("dataAccess")
                        .containsExactly(new ObjectToMapConverter().asMap(new DataAccess(DataAccessTypes.MANAGED))));
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
            String projectMetadataUrl = getProjectMetadataUrl(metadataTypePlural, makeUuid("a"));

            webApp.perform(get(projectMetadataUrl))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        @WithMockUser(
                username = "alice",
                roles = {"CONTRIBUTOR", "access_aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"})
        public void userOnProjectAList_CanSeeOpenProjectMetadata(String metadataTypePlural) throws Exception {
            String openAccessProjectMetadataUrl = getProjectMetadataUrl(metadataTypePlural, makeUuid("c"));

            webApp.perform(get(openAccessProjectMetadataUrl).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        @WithMockUser(
                username = "bob",
                roles = {"CONTRIBUTOR", "access_bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"})
        public void userNotOnProjectAList_CannotSeeMetadata(String metadataTypePlural) throws Exception {
            String projectMetadataUrl = getProjectMetadataUrl(metadataTypePlural, makeUuid("a"));

            webApp.perform(get(projectMetadataUrl))
                    .andExpect(status().isForbidden());
        }
    }

    @NotNull
    private String getProjectMetadataUrl(String metadataTypePlural, String uuid) {
        return projectRepository.findByUuid(new Uuid(uuid))
                .findFirst()
                .map(Project::getId)
                .map(projectId -> String.format("/projects/%s/%s", projectId, metadataTypePlural))
                .get();
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
                                                   List<Class<? extends MetadataDocument>> metadataTypes) {
        Project project = projectRepository.findByUuid(new Uuid(uuidString))
                .findFirst().get();
        try {
            final String submissionUrl = createSubmissionAndGetUrl();
            linkSubmissionToProject(project, submissionUrl);
            metadataTypes.forEach(metadataType -> {
                try {
                    MetadataDocument metadataDocument = newMetadataInstance(metadataType);
                    String lowerCaseMetadataType = metadataDocument.getType().toString().toLowerCase();
                    setDocumentProperties(uuidString, metadataDocument);

                    String submissionMetadataDocumentsUrl = buildSubmissionMetadataUrl(submissionUrl, lowerCaseMetadataType);
                    Map documentAsMap = new ObjectToMapConverter(objectMapper)
                            .asMap(metadataDocument, List.of("contentLastModified"));
                    webApp.perform(post(submissionMetadataDocumentsUrl)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(mapAsJsonString(documentAsMap)))
                            .andExpect(status().isAccepted());
                    resetTestingSecurityContext();
                } catch (Exception e) {
                    throw new RuntimeException("problem crating metadata document " + metadataType.getSimpleName(), e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildSubmissionMetadataUrl(String submissionUrl, String lowerCaseMetadataType) {
        return submissionUrl + "/" + lowerCaseMetadataType + (lowerCaseMetadataType.endsWith("s")?"es":"s");
    }

    @NotNull
    private static void setDocumentProperties(String uuidString, MetadataDocument metadataDocument) {
        String lowerCaseMetadataType = metadataDocument.getType().toString().toLowerCase();
        Map<String, Map<String, String>> content = Map.of(lowerCaseMetadataType + "_core", Map.of(lowerCaseMetadataType + "_name", lowerCaseMetadataType + " in project " + uuidString));
        metadataDocument.setContent(content);
        metadataDocument.setUuid(Uuid.newUuid());
    }

    @NotNull
    private static MetadataDocument newMetadataInstance(Class<? extends MetadataDocument> metadataType) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        MetadataDocument metadataDocument;
        try {
            // try the single arg ctor with null content arg
            // content will be set later
            metadataDocument = metadataType.getConstructor(Object.class).newInstance(new Object[]{ null });
        } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException |
                 SecurityException e) {
            // fallback to no-args ctor
            metadataDocument = metadataType.getConstructor().newInstance();
        }
        return metadataDocument;
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


    @Nested
    class MetadataFromSubmissionAccessControl {
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProject")
        @WithMockUser(
                username = "alice",
                roles = {"CONTRIBUTOR", "access_aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"})
        public void userOnProjectAList_CanSeeSubmissionMetadata(String metadataTypePlural) throws Exception {
            String submissionMetadataUrl = getSubmissionMetadataUrl(metadataTypePlural, makeUuid("a"));
            webApp.perform(get(submissionMetadataUrl))
                    .andExpect(status().isOk());
        }
    }

    @NotNull
    private String getSubmissionMetadataUrl(String metadataTypePlural, String uuid) {
        return projectRepository.findByUuid(new Uuid(uuid))
                .map(Project::getSubmissionEnvelopes)
                .flatMap(Collection::stream)
                .map(AbstractEntity::getId)
                .map(submissionId -> String.format("/submissionEnvelopes/%s/%s", submissionId, metadataTypePlural))
                .findFirst()
                .get();
    }

    @Nested
    @WithMockUser(
            username = "service",
            roles = {"SERVICE"})
    class ServiceUserAccessControl {
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypesWithProject")
        public void serviceUser_CanSeeSubmissionMetadata(String metadataTypePlural) throws Exception {
            String submissionMetadataUrl = getSubmissionMetadataUrl(metadataTypePlural, makeUuid("a"));
            webApp.perform(get(submissionMetadataUrl))
                    .andExpect(status().isOk());
        }

        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        public void serviceUser_CanSeeProjectMetadata(String metadataTypePlural) throws Exception {
            String projectMetadataUrl = getProjectMetadataUrl(metadataTypePlural, makeUuid("a"));
            webApp.perform(get(projectMetadataUrl))
                    .andExpect(status().isOk());
        }
        @ParameterizedTest
        @MethodSource("org.humancellatlas.ingest.security.SecurityTest#metadataTypes")
        public void serviceUser_CanSeeOnlyOpenAndProjectAMetadata(String metadataTypePlural) throws Exception {
            String metadataCollectionUrl = "/" + metadataTypePlural;
            webApp.perform(get(metadataCollectionUrl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page.totalElements").value("3"));
        }
    }
}
