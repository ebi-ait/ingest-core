package org.humancellatlas.ingest.project;

import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.project.web.SearchFilter;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
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


    @Test
    void testSearchFilter() {
        assertThat(mongoTemplate).isNotNull();

        // given
        this.mongoTemplate.save(makeTestProject("project1"));
        this.mongoTemplate.save(makeTestProject("project2"));
        this.mongoTemplate.save(makeTestProject("project3"));

        //when
        SearchFilter searchFilter = new SearchFilter("project1", null, null);
        Pageable pageable = PageRequest.of(1,10);
        Page<Project> result = projectService.filterProjects(searchFilter, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @NotNull
    private Project makeTestProject(String title) {
        Map content = new HashMap<String, Object>();
        content.put("project_core", Map.of("project_title", title));
        Project project = new Project(content);
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
    }

}
