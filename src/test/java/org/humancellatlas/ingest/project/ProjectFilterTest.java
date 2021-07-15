package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.project.web.SearchFilter;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest()
class ProjectFilterTest {

    // class under test
    private ProjectService projectService;

    // participants
    @Autowired
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

    private Project project1;

    Comparator<Instant> closeEnough = new Comparator<Instant>() {
        public int compare(Instant d1, Instant d2) {
            return d1.truncatedTo(ChronoUnit.MILLIS).compareTo(d2.truncatedTo(ChronoUnit.MILLIS));
        }
    };

    @Test
    void test_raw_criteria() {
        Query query = new Query().addCriteria(Criteria.where("content.project_core.project_title").regex("project1", "i"));
        Project actual = this.mongoTemplate.find(query, Project.class).get(0);
        assertThat(actual)
                .usingComparatorForFields(closeEnough,"contentLastModified")
                .isEqualToComparingFieldByFieldRecursively(project1);

    }

    @Test
    void test_criteria_building() {
        SearchFilter searchFilter = new SearchFilter("project1", null, null);
        Query query = projectService.buildProjectsQuery(searchFilter);
        Project actual = this.mongoTemplate.find(query, Project.class).get(0);
        assertThat(actual)
                .usingComparatorForFields(closeEnough,"contentLastModified")
                .isEqualToComparingFieldByFieldRecursively(project1);

    }

    @Test
    void filter_by_text() {

        // given
        //when
        SearchFilter searchFilter = new SearchFilter("project1", null, null);

        Pageable pageable = PageRequest.of(1, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).containsExactly(project1);
    }

    private static Project makeProject(String title) {
        Map content = Map.of("project_core",
                Map.of("project_title", title));
        Project project = new Project(content);
        project.setIsUpdate(false);
        return project;
    }


    @BeforeEach
    private void setup() {
        this.projectService = new ProjectService(
                mongoTemplate,
                submissionEnvelopeRepository,
                projectRepository,
                metadataCrudService,
                metadataUpdateService,
                schemaService,
                bundleManifestRepository,
                projectEventHandler);
        assertThat(this.mongoTemplate.findAll(Project.class)).hasSize(0);
        this.project1 = makeProject("project1");
        this.mongoTemplate.save(project1);
        this.mongoTemplate.save(makeProject("project2"));
        this.mongoTemplate.save(makeProject("project3"));
        assertThat(this.mongoTemplate.findAll(Project.class)).hasSize(3);
    }

    @AfterEach
    private void tearDown() {
        this.mongoTemplate.dropCollection(Project.class);
    }
}
