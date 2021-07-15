package org.humancellatlas.ingest.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.assertj.core.api.Assertions;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.project.exception.NonEmptyProject;
import org.humancellatlas.ingest.project.web.SearchFilter;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        ProjectService.class,
        ProjectRepository.class,
        SubmissionEnvelopeRepository.class
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

    @BeforeEach
    void setUp() {
        applicationContext.getBeansWithAnnotation(MockBean.class).forEach(Mockito::reset);
    }

    @Nested
    class SubmissionEnvelopes {

        @Test
        @DisplayName("get all submissions")
        void getFromAllCopiesOfProjects() {
            //given:
            var project1 = new Project("project");
            var submissionSet1 = IntStream.range(0, 3)
                    .mapToObj(Integer::toString)
                    .map(SubmissionEnvelope::new)
                    .collect(toSet());

            submissionSet1.forEach(project1::addToSubmissionEnvelopes);

            //and:
            var project2 = new Project("project2");
            BeanUtils.copyProperties(project1, project2);
            var submissionSet2 = IntStream.range(10, 15)
                    .mapToObj(Integer::toString)
                    .map(SubmissionEnvelope::new)
                    .collect(toSet());
            submissionSet2.forEach(project2::addToSubmissionEnvelopes);

            //and:
            doReturn(Stream.of(project1, project2))
                    .when(projectRepository).findByUuid(project1.getUuid());

            //when:
            var submissionEnvelopes = projectService.getSubmissionEnvelopes(project1);

            //then:
            assertThat(submissionEnvelopes)
                    .containsAll(submissionSet1)
                    .containsAll(submissionSet2);
        }

        @Test
        @DisplayName("no duplicate submissions")
        void getFromAllCopiesOfProjectsNoDuplicates() {
            //given:
            var project1 = new Project("project1");
            var submissionSet1 = IntStream.range(0, 3)
                .mapToObj(Integer::toString)
                .map(SubmissionEnvelope::new)
                .collect(toSet());
            submissionSet1.forEach(project1::addToSubmissionEnvelopes);

            //and:
            var project2 = new Project("project2");
            BeanUtils.copyProperties(project1, project2);
            var submissionSet2 = IntStream.range(10, 15)
                .mapToObj(Integer::toString)
                .map(SubmissionEnvelope::new)
                .collect(toSet());
            submissionSet2.forEach(project2::addToSubmissionEnvelopes);

            var project3 = new Project(null);
            BeanUtils.copyProperties(project1, project3);
            submissionSet1.forEach(submission ->
                project3.addToSubmissionEnvelopes(new SubmissionEnvelope(submission.getId()))
            );

            var documentIds = new ArrayList<String>();
            submissionSet1.forEach(submission -> documentIds.add(submission.getId()));
            submissionSet2.forEach(submission -> documentIds.add(submission.getId()));

            //and:
            doReturn(Stream.of(project1, project2, project3))
                .when(projectRepository).findByUuid(project1.getUuid());

            //when:
            var submissionEnvelopes = projectService.getSubmissionEnvelopes(project1);
            var returnDocumentIds = new ArrayList<String>();
            submissionEnvelopes.forEach(submission -> returnDocumentIds.add(submission.getId()));

            //then:
            assertThat(returnDocumentIds)
                .containsExactlyInAnyOrderElementsOf(documentIds);
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
            persistentProjects.forEach(persistentProject ->
                verify(projectRepository).delete(persistentProject)
            );
        }

        @Test
        @DisplayName("fails for non-empty Project")
        void failForProjectWithSubmissions() {
            //given:
            var project = new Project("test project");

            //and: copy of project with no submissions
            var persistentEmptyProject = new Project("project");
            BeanUtils.copyProperties(project, persistentEmptyProject);

            //and: copy of project with submissions
            var persistentNonEmptyProject = new Project("project");
            BeanUtils.copyProperties(project, persistentNonEmptyProject);
            IntStream.range(0, 3)
                    .mapToObj(Integer::toString).map(SubmissionEnvelope::new)
                    .forEach(persistentNonEmptyProject::addToSubmissionEnvelopes);

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
    class FilterProject {
        private String primaryWranglerKey = "primaryWrangler";
        private String isUpdateKey = "isUpdate";
        private String wranglingStateKey = "wranglingState";
        private String projectTitleKey = "content.project_core.project_title";
        private String projectDescripionKey = "content.project_core.project_description";
        private String projectShortNameKey = "content.project_core.project_short_name";

        private String searchText = "search text";
        private String wranglingStateValue = "NEW";
        private String primaryWranglerValue = "primary wrangler 1";

        Criteria getDefaultFilter() {
            return Criteria.where(isUpdateKey).is(false);
        }

        Criteria filterByWrangler(String wrangler) {
            return Criteria.where(primaryWranglerKey).is(wrangler);
        }

        Criteria filterByWranglingState(String wranglingState) {
            return Criteria.where(wranglingStateKey).is(wranglingState);
        }

        Criteria filterBySearch(String search) {
            return new Criteria().orOperator(
                    Criteria.where(projectTitleKey).regex(search, "i"),
                    Criteria.where(projectDescripionKey).regex(search, "i"),
                    Criteria.where(projectShortNameKey).regex(search, "i")
            );
        }

        Query buildQuery(Criteria[] criteria_list) {
            return new Query().addCriteria(new Criteria().andOperator(criteria_list));
        }

        @Test
        @DisplayName("get all projects")
        void getProjectsWithNoFilter() {
            // given empty search filter
            SearchFilter searchFilter = new SearchFilter();
            Query expectedResult = projectService.buildProjectsQuery(searchFilter);

            Query actualResult = this.buildQuery(new Criteria[]{getDefaultFilter()});
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("filter projects by primary wrangler")
        void getProjectsByPrimaryWrangler() {

            SearchFilter searchFilter = new SearchFilter(null, null, primaryWranglerValue);
            Query expectedResult = projectService.buildProjectsQuery(searchFilter);

            Query actualResult = this.buildQuery(new Criteria[]{getDefaultFilter(), filterByWrangler(primaryWranglerValue)});
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("filter projects by wrangling state")
        void getProjectsByWranglingState() {
            SearchFilter searchFilter = new SearchFilter(null, wranglingStateValue, null);
            Query expectedResult = projectService.buildProjectsQuery(searchFilter);

            Query actualResult = this.buildQuery(new Criteria[]{getDefaultFilter(), filterByWranglingState(wranglingStateValue)});
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("filter projects by search text")
        void getProjectsBySearchText() {

            SearchFilter searchFilter = new SearchFilter(searchText, null, null);
            Query expectedResult = projectService.buildProjectsQuery(searchFilter);

            Query actualResult = this.buildQuery(new Criteria[]{getDefaultFilter(), filterBySearch(searchText)});
            assertThat(expectedResult.getQueryObject()).isEqualTo(actualResult.getQueryObject());
        }

        @Test
        @DisplayName("filter projects by search text and wrangler")
        void getProjectsBySearchTextAndWrangler() {

            SearchFilter searchFilter = new SearchFilter(searchText, null, primaryWranglerValue);
            Query expectedResult = projectService.buildProjectsQuery(searchFilter);

            Query actualResult = this.buildQuery(new Criteria[]{getDefaultFilter(), filterBySearch(searchText), filterByWrangler(primaryWranglerValue)});
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("filter projects by search text, wrangler and wrangling state")
        void getProjectsBySearchTextWranglerAndWranglingState() {

            SearchFilter searchFilter = new SearchFilter(searchText, wranglingStateValue, primaryWranglerValue);
            Query expectedResult = projectService.buildProjectsQuery(searchFilter);

            Query actualResult = this.buildQuery(new Criteria[]{getDefaultFilter(), filterBySearch(searchText), filterByWrangler(primaryWranglerValue), filterByWranglingState(wranglingStateValue)});
            assertEquals(expectedResult, actualResult);

        }

        @Test
        @DisplayName("filter projects by search text and wrangling state")
        void getProjectsBySearchTextAndWranglingState() {

            SearchFilter searchFilter = new SearchFilter(searchText, wranglingStateValue, null);
            Query expectedResult = projectService.buildProjectsQuery(searchFilter);

            Query actualResult = this.buildQuery(new Criteria[]{getDefaultFilter(), filterBySearch(searchText), filterByWranglingState(wranglingStateValue)});
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("filter projects by wrangling and wrangling state")
        void getProjectsByWranglerAndWranglingState() {

            SearchFilter searchFilter = new SearchFilter(null, wranglingStateValue, primaryWranglerValue);
            Query expectedResult = projectService.buildProjectsQuery(searchFilter);

            Query actualResult = this.buildQuery(new Criteria[]{getDefaultFilter(),filterByWranglingState(wranglingStateValue),filterByWrangler(primaryWranglerValue)});
            assertEquals(expectedResult, actualResult);

        }
    }
}
