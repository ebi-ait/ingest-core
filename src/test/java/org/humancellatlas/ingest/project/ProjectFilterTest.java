package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.project.web.SearchFilter;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
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
    private Project project2;
    private Project project3;

    Comparator<Instant> upToMillies = new Comparator<Instant>() {
        public int compare(Instant d1, Instant d2) {
            return d1.truncatedTo(ChronoUnit.MILLIS).compareTo(d2.truncatedTo(ChronoUnit.MILLIS));
        }
    };

    @Test
    void test_raw_criteria() {
        Query query = new Query().addCriteria(Criteria.where("content.project_core.project_title").regex("project1", "i"));
        Project actual = this.mongoTemplate.findOne(query, Project.class);
        assertThat(actual)
                .usingComparatorForFields(upToMillies, "contentLastModified")
                .isEqualToComparingFieldByFieldRecursively(project1);

    }

    @Test
    void test_criteria_building() {
        SearchFilter searchFilter = new SearchFilter(null, "NEW", null);
        Query query = ProjectQueryBuilder.buildProjectsQuery(searchFilter);
        Project actual = this.mongoTemplate.find(query, Project.class).get(0);
        assertThat(actual)
                .usingComparatorForFields(upToMillies, "contentLastModified")
                .isEqualToComparingFieldByFieldRecursively(project1);
    }

    @Test
    void test_criteria_building_with_pageable() {
        SearchFilter searchFilter = new SearchFilter("project1", null, null);
        Query query = ProjectQueryBuilder.buildProjectsQuery(searchFilter);
        Pageable pageable = PageRequest.of(0, 10);
        Project actual = this.mongoTemplate.find(query.with(pageable), Project.class).get(0);
        assertThat(actual)
                .usingComparatorForFields(upToMillies, "contentLastModified")
                .isEqualToComparingFieldByFieldRecursively(project1);
    }

    @Test
    void filter_by_state() {
        Project project4 = makeProject("project4");
        project4.setWranglingState(WranglingState.IN_PROGRESS);
        this.mongoTemplate.save(project4);

        //when
        SearchFilter filterNew = new SearchFilter(null, "NEW", null);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(filterNew, pageable);

        // then
        assertThat(result.getContent())
                .hasSize(3)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(project1, project2, project3);
    }

    @Test
    void filter_by_wrangler() {
        // given
        //when
        SearchFilter searchFilter = new SearchFilter(null, null, "wrangler_project2");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(this.project2);
    }

    @Test
    void filter_by_text() {
        // given
        //when
        SearchFilter searchFilter = new SearchFilter("project1", null, null);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingElementComparatorIgnoringFields("supplementaryFiles", "submissionEnvelopes")
                .containsExactly(project1);
    }

    private static Project makeProject(String title) {
        Map content = Map.of("project_core",
                Map.of("project_title", title));
        Project project = new Project(content);
        project.setIsUpdate(false);
        project.setPrimaryWrangler("wrangler_" + title);
        project.setWranglingState(WranglingState.NEW);
        return project;
    }


    @BeforeEach
    private void setup() {
        initProjectService();
        initTestData();
    }

    private void initTestData() {
        this.project1 = makeProject("project1");
        this.project2 = makeProject("project2");
        this.project3 = makeProject("project3");
        Arrays.asList(project1, project2, project3).forEach(project -> {
            this.mongoTemplate.save(project);
            this.projectService.register(project);
        });
        this.mongoTemplate.indexOps(Project.class).ensureIndex(
                new TextIndexDefinition.TextIndexDefinitionBuilder()
                        .onField("content.project_core.project_title")
                        .build()
        );
        assertThat(this.mongoTemplate.findAll(Project.class)).hasSize(3);
    }

    private void initProjectService() {
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
    }

    @AfterEach
    private void tearDown() {
        this.mongoTemplate.dropCollection(Project.class);
    }
}
