package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.audit.AuditEntry;
import org.humancellatlas.ingest.audit.AuditEntryService;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.project.exception.NonEmptyProject;
import org.humancellatlas.ingest.schemas.Schema;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.humancellatlas.ingest.audit.AuditType.STATUS_UPDATED;
import static org.humancellatlas.ingest.project.WranglingState.ELIGIBLE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        ProjectService.class,
        ProjectRepository.class,
        SubmissionEnvelopeRepository.class,
})
public class ProjectServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProjectService projectService;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private SubmissionEnvelopeRepository submissionEnvelopeRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private MetadataCrudService metadataCrudService;

    @MockBean
    private MetadataUpdateService metadataUpdateService;

    @MockBean
    private SchemaService schemaService;

    @MockBean
    private BundleManifestRepository bundleManifestRepository;

    @MockBean
    private ProjectEventHandler projectEventHandler;

    @MockBean
    private AuditEntryService auditEntryService;

    @BeforeEach
    void setUp() {
        applicationContext.getBeansWithAnnotation(MockBean.class).forEach(Mockito::reset);
    }

    @Nested
    class SubmissionEnvelopes {
        Project project1;
        Project project2;
        Set<SubmissionEnvelope> submissionSet1;
        Set<SubmissionEnvelope> submissionSet2;

        @BeforeEach
        void setup(){
            // given
            project1 = spy(new Project(null));
            doReturn("project1").when(project1).getId();
            project1.setUuid(Uuid.newUuid());

            submissionSet1 = new HashSet<>();
            IntStream.range(0, 3).mapToObj(Integer::toString).forEach(id -> {
                var sub = spy(new SubmissionEnvelope());
                doReturn(id).when(sub).getId();
                submissionSet1.add(sub);
            });
            submissionSet1.forEach(project1::addToSubmissionEnvelopes);

            //and:
            project2 = spy(new Project(null));
            doReturn("project2").when(project2).getId();
            project2.setUuid(project1.getUuid());

            submissionSet2 = new HashSet<>();
            IntStream.range(10, 15).mapToObj(Integer::toString).forEach(id -> {
                var sub = spy(new SubmissionEnvelope());
                doReturn(id).when(sub).getId();
                submissionSet2.add(sub);
            });
            submissionSet2.forEach(project2::addToSubmissionEnvelopes);
        }

        @Test
        @DisplayName("get all submissions")
        void getFromAllCopiesOfProjects() {
            // given
            when(projectRepository.findByUuid(project1.getUuid())).thenReturn(Stream.of(project1, project2));

            // when:
            var submissionEnvelopes = projectService.getSubmissionEnvelopes(project1);

            //then:
            assertThat(submissionEnvelopes)
                    .containsAll(submissionSet1)
                    .containsAll(submissionSet2);
        }

        @Test
        @DisplayName("no duplicate submissions")
        void getFromAllCopiesOfProjectsNoDuplicates() {
            // given
            var project3 = spy(new Project(null));
            doReturn("project3").when(project3).getId();
            project3.setUuid(project1.getUuid());

            submissionSet1.forEach(project3::addToSubmissionEnvelopes);

            var documentIds = new ArrayList<String>();
            submissionSet1.forEach(submission -> documentIds.add(submission.getId()));
            submissionSet2.forEach(submission -> documentIds.add(submission.getId()));

            //and:
            when(projectRepository.findByUuid(project1.getUuid())).thenReturn(Stream.of(project1, project2, project3));

            //when:
            var submissionEnvelopes = projectService.getSubmissionEnvelopes(project1);

            //then:
            var returnDocumentIds = new ArrayList<String>();
            submissionEnvelopes.forEach(submission -> returnDocumentIds.add(submission.getId()));

            assertThat(returnDocumentIds).containsExactlyInAnyOrderElementsOf(documentIds);
        }

    }

    @Nested
    class Registration {

        @Test
        @DisplayName("success")
        void succeed() {
            //given:
            String content = "{\"name\": \"project\"}";
            Project project = new Project(content);

            //and:
            Project persistentProject = new Project(content);
            doReturn(persistentProject).when(projectRepository).save(project);

            //when:
            Project result = projectService.register(project);

            //then:
            verify(projectRepository).save(project);
            assertThat(result).isEqualTo(persistentProject);
            verify(projectEventHandler).registeredProject(persistentProject);
        }

    }

    @Nested
    class Deletion {

        @Test
        @DisplayName("success")
        void succeedForEmptyProject() throws Exception {
            //given:
            var project = new Project("{\"name\": \"test\"}");

            //and:
            var persistentProjects = IntStream.range(0, 3)
                    .mapToObj(number -> new Project(null))
                    .collect(toList());
            persistentProjects.forEach(persistentProject ->
                BeanUtils.copyProperties(project, persistentProject)
            );
            doReturn(persistentProjects.stream())
                    .when(projectRepository).findByUuid(project.getUuid());

            //when:
            projectService.delete(project);

            //then:
            persistentProjects.forEach(persistentProject -> {
                verify(metadataCrudService).deleteDocument(persistentProject);
                verify(projectEventHandler).deletedProject(persistentProject);
            });
        }

        @Test
        @DisplayName("fails for non-empty Project")
        void failForProjectWithSubmissions() {
            //given:
            var project = new Project(null);

            //and: copy of project with no submissions
            var persistentEmptyProject = new Project(null);
            BeanUtils.copyProperties(project, persistentEmptyProject);

            //and: copy of project with submissions
            var persistentNonEmptyProject = new Project(null);
            BeanUtils.copyProperties(project, persistentNonEmptyProject);
            IntStream.range(0, 3)
                    .mapToObj(Integer::toString)
                    .forEach(id -> {
                        SubmissionEnvelope sub = spy(new SubmissionEnvelope());
                        doReturn(id).when(sub).getId();
                        persistentNonEmptyProject.addToSubmissionEnvelopes(sub);
                    });

            //and:
            doReturn(Stream.of(persistentEmptyProject, persistentNonEmptyProject))
                    .when(projectRepository).findByUuid(project.getUuid());

            //expect:
            Assertions.assertThatThrownBy(() ->
                projectService.delete(project)
            ).isInstanceOf(NonEmptyProject.class);
        }

    }

    @Nested
    class SuggestedProject {

        @Test
        @DisplayName("Register a project")
        void givenSuggestionCreatesProjectSuccessfully() {
            //given:
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode suggestion = mapper.createObjectNode();
            suggestion.put("doi", "doi123");
            suggestion.put("name", "Test User");
            suggestion.put("email", "test@example.com");
            suggestion.put("comments", "This is a comment");

            //and:
            final String highLevelEntity = "type";
            Schema projectSchema = new Schema(highLevelEntity, "2.0","project", "project", "project", "mock.io/mock-schema-project");
            doReturn(projectSchema).when(schemaService).getLatestSchemaByEntityType(highLevelEntity, "project");

            Map<String, String> content = new HashMap<>();
            final String entityType = "project";
            content.put("describedBy", schemaService.getLatestSchemaByEntityType(highLevelEntity, entityType).getSchemaUri());
            content.put("schema_type", entityType);

            Project persistentProject = new Project(content);
            persistentProject.setWranglingState(WranglingState.NEW_SUGGESTION);
            persistentProject.setWranglingNotes(
                String.format(
                    "DOI: %s \nName: %s \nEmail: %s \nComments: %s",
                    suggestion.get("doi"),
                    suggestion.get("name"),
                    suggestion.get("email"),
                    suggestion.get("comments")
                )
            );
            doReturn(persistentProject).when(projectRepository).save(any(Project.class));


            //when:
            Project result = projectService.createSuggestedProject(suggestion);

            //then:
            assertThat(result).isEqualTo(persistentProject);
        }
    }

    @Nested
    class ProjectUpdate {

        @Test
        @DisplayName("state update adds a history entry")
        void statusUpdatesAddsHistoryRecord() {
            // given
            Project project = new Project(null);

            // when
            projectService.updateWranglingState(project, ELIGIBLE);

            // then
            verify(auditEntryService)
                    .addAuditEntry(new AuditEntry(STATUS_UPDATED,
                            any(),
                            ELIGIBLE,
                            project));
        }
    }
}
