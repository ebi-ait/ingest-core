package org.humancellatlas.ingest.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.project.*;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;


import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class AuditEntryTest {

    private ProjectService projectService;

    private AuditEntryService auditEntryService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AuditEntryRepository auditEntryRepository;

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
    private void setup() {
        initAuditEntryService();
        initProjectService();
    }


    @Test
    void testAuditEntryGenerationOnProjectStateUpdate() {
        //given
        WranglingState initialWranglingState = WranglingState.NEW;
        Project project = new Project("{\"name\": \"Project 1\"}");
        project.setWranglingState(initialWranglingState);
        this.mongoTemplate.save(project);

        // when
        WranglingState updatedWranglingState = WranglingState.ELIGIBLE;
        ObjectNode patchUpdate = new ObjectMapper().createObjectNode();
        patchUpdate.put("wranglingState", updatedWranglingState.getValue());
        projectService.update(project, patchUpdate, false);

        // then
        AuditEntry actual = projectService.getProjectAuditEntry(project).get(0);

        assertThat(actual.getAuditType()).isEqualTo(AuditType.STATUS_UPDATED);
        assertThat(actual.getBefore()).isEqualTo(initialWranglingState.name());
        assertThat(actual.getAfter()).isEqualTo(updatedWranglingState.name());
    }

    private void initAuditEntryService() {
        this.auditEntryService = new AuditEntryService(auditEntryRepository);
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
                auditEntryService,
                projectEventHandler
        );
    }

    @AfterEach
    private void tearDown() {
        this.mongoTemplate.dropCollection(Project.class);
        this.mongoTemplate.dropCollection(AuditEntry.class);

    }
}
