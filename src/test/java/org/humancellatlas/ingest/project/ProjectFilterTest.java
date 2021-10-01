package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.project.web.SearchFilter;
import org.humancellatlas.ingest.project.web.SearchType;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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

    Comparator<Instant> upToMillies = Comparator.comparing(d -> d.truncatedTo(ChronoUnit.MILLIS));

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
        SearchFilter searchFilter = SearchFilter.builder().wranglingState("NEW").build();
        Query query = ProjectQueryBuilder.buildProjectsQuery(searchFilter);
        Project actual = this.mongoTemplate.find(query, Project.class).get(0);
        assertThat(actual)
                .usingComparatorForFields(upToMillies, "contentLastModified")
                .isEqualToComparingFieldByFieldRecursively(project1);
    }

    @Test
    void test_criteria_building_with_pageable() {
        SearchFilter searchFilter = SearchFilter.builder().search("project1").build();
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
        SearchFilter filterNew = SearchFilter.builder().wranglingState("NEW").build();

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
        SearchFilter searchFilter = SearchFilter.builder().primaryWrangler(this.project2.getPrimaryWrangler()).build();

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
    void filter_by_wrangling_priority() {
        //given
        Project project4 = makeProject("project4");
        project4.setWranglingPriority(3);
        this.mongoTemplate.save(project4);
        //when
        SearchFilter searchFilter = SearchFilter.builder().wranglingPriority(this.project1.getWranglingPriority()).build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .hasSize(3)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(project1, project2, project3);
    }

    @Test
    void filter_by_official_hca_publication() {
        //given
        Project project4 = makeProject("project4");
        var content = Map.of(
                "project_core", Map.of("project_title", "Project 4"),
                "publications", List.of(Map.of("official_hca_publication", true))
        );
        project4.setContent(content);
        this.mongoTemplate.save(project4);

        //when
        SearchFilter searchFilter = SearchFilter.builder().hasOfficialHcaPublication(true).build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(project4);
    }

    @Test
    void filter_by_identifying_organisms() {
        //given
        Project project4 = makeProject("project4");
        String human = "Human";
        project4.setIdentifyingOrganisms(List.of(human));
        this.mongoTemplate.save(project4);

        //when
        SearchFilter searchFilter = SearchFilter.builder().identifyingOrganism(human).build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(project4);
    }

    @Test
    void filter_by_organ_ontology() {
        //given
        Project project4 = makeProject("project4");
        String ontologyTerm = "AN_ONTOLOGY";
        project4.setOrgan(Map.of("ontologies", List.of(Map.of("ontology", ontologyTerm))));
        this.mongoTemplate.save(project4);
        //when
        SearchFilter searchFilter = SearchFilter.builder().organOntology(ontologyTerm).build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(project4);
    }

    @Test
    void filter_by_cell_count() {
        //given
        Project project4 = makeProject("project4");
        project4.setCellCount(1000);
        this.mongoTemplate.save(project4);
        //when
        SearchFilter searchFilter = SearchFilter.builder().maxCellCount(project1.getCellCount()).minCellCount(0).build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent())
                .hasSize(3)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(project1, project2, project3);
    }

    @Test
    void filter_by_data_access() {
        //given
        Project project4 = makeProject("project4");
        project4.setDataAccess(Map.of("type", DataAccessTypes.OPEN.toString()));
        this.mongoTemplate.save(project4);
        //when
        SearchFilter searchFilter = SearchFilter.builder().dataAccess(DataAccessTypes.OPEN).build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(project4);
    }

    @Test
    void filter_by_text() {
        // given
        //when
        SearchFilter searchFilter = SearchFilter.builder().search("project1").build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingElementComparatorIgnoringFields("supplementaryFiles", "submissionEnvelopes")
                .containsExactly(project1);
    }

    @Test
    void query_all_keywords(){
        SearchFilter searchFilter = SearchFilter.builder().search("human liver").build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingElementComparatorIgnoringFields("supplementaryFiles", "submissionEnvelopes")
                .containsExactly(project1);
    }
    @Test
    void query_all_keywords__order_independent(){
        SearchFilter searchFilter = SearchFilter.builder()
                .search("liver human")
                .searchType(SearchType.AllKeywords)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingElementComparatorIgnoringFields("supplementaryFiles", "submissionEnvelopes")
                .containsExactly(project1);
    }
    @Test
    void query_any_keywords(){
        SearchFilter searchFilter = SearchFilter.builder()
                .search("liver mouse")
                .searchType(SearchType.AnyKeyword)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);
        Page<Project> resultFromReverse = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getContent())
                .hasSize(2)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingElementComparatorIgnoringFields("supplementaryFiles", "submissionEnvelopes")
                .containsExactly(project1, project2);
    }
    @Test
    void query_exact_phrase__correct_order(){
        SearchFilter searchFilter = SearchFilter.builder()
                .search("human liver")
                .searchType(SearchType.ExactMatch)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);
        Page<Project> resultFromReverse = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingElementComparatorIgnoringFields("supplementaryFiles", "submissionEnvelopes")
                .containsExactly(project1);
    }

    @Test
    void query_exact_phrase__reverse_order(){
        SearchFilter searchFilter = SearchFilter.builder()
                .search("liver human")
                .searchType(SearchType.ExactMatch)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getContent())
                .hasSize(0);
    }

    @Test
    void lookup_by_uuid() {
        String uuidString = project1.getUuid().getUuid().toString();
        SearchFilter searchFilter = SearchFilter.builder()
                .search(uuidString)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getContent())
                .hasSize(1)
                .usingComparatorForElementFieldsWithType(upToMillies, Instant.class)
                .usingElementComparatorIgnoringFields("supplementaryFiles", "submissionEnvelopes")
                .containsExactly(project1);
    }

    @Test
    void all_args_constructor() {
        new SearchFilter(
                "a",
                "b",
                "c",
                1,
                false,
                "Human",
                "AN_ONTOLOGY_TERM",
                0,
                10000,
                DataAccessTypes.MANAGED,
                SearchType.AllKeywords
        );
    }

    private static Project makeProject(String title) {
        Map<String, Map<String, String>> content = Map.of("project_core",
                Map.of("project_title", title));
        Project project = new Project(content);
        project.setIsUpdate(false);
        project.setPrimaryWrangler("wrangler_" + title);
        project.setWranglingState(WranglingState.NEW);
        project.setUuid(Uuid.newUuid());
        project.setCellCount(100);
        project.setWranglingPriority(1);
        return project;
    }

    @BeforeEach
    private void setup() {
        initProjectService();
        initTestData();
    }

    private void initTestData() {
        this.project1 = makeProject("project1 human liver");
        this.project2 = makeProject("project2 mouse liver");
        this.project3 = makeProject("project3 lung human");
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
