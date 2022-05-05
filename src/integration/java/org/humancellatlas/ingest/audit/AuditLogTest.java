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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
public class AuditLogTest {

    private ProjectService projectService;

    private AuditLogService auditLogService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AuditLogRepository auditLogRepository;

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


    Comparator<Instant> upToMillies = Comparator.comparing(d -> d.truncatedTo(ChronoUnit.MILLIS));

    @BeforeEach
    private void setup() {
        initAuditLogService();
        initProjectService();
    }


    @Test
    void testAuditLogGenerationOnProjectStateUpdate() {
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
        AuditLog actual = projectService.getProjectAuditLog(project).get(0);
        AuditLog expected = new AuditLog(AuditType.STATUS_UPDATED, initialWranglingState, updatedWranglingState, project);
        assertThat(actual)
                .usingComparatorForFields((x,y)->0 )
                .isEqualToComparingOnlyGivenFields(expected, "auditType", "before", "after");

    }

    private void initAuditLogService() {
        this.auditLogService = new AuditLogService(auditLogRepository);
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
                auditLogService,
                projectEventHandler
        );
    }

    @AfterEach
    private void tearDown() {
        this.mongoTemplate.dropCollection(Project.class);
        this.mongoTemplate.dropCollection(AuditLog.class);

    }
}
