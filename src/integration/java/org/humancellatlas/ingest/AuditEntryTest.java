package org.humancellatlas.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import org.humancellatlas.ingest.audit.AuditEntry;
import org.humancellatlas.ingest.audit.AuditEntryRepository;
import org.humancellatlas.ingest.audit.AuditEntryService;
import org.humancellatlas.ingest.audit.AuditType;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.project.*;
import org.humancellatlas.ingest.schemas.SchemaService;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringBootTest
public class AuditEntryTest {
  @Autowired private ProjectService projectService;

  @Autowired private AuditEntryService auditEntryService;

  @Autowired private MongoTemplate mongoTemplate;

  @Autowired private AuditEntryRepository auditEntryRepository;

  @MockBean private SubmissionEnvelopeRepository submissionEnvelopeRepository;

  @MockBean private ProjectRepository projectRepository;

  @MockBean private MetadataCrudService metadataCrudService;

  @MockBean private MetadataUpdateService metadataUpdateService;

  @MockBean private SchemaService schemaService;

  @MockBean private BundleManifestRepository bundleManifestRepository;

  @MockBean private ProjectEventHandler projectEventHandler;

  @MockBean MigrationConfiguration migrationConfiguration;

  @Test
  @WithMockUser(value = "test_user")
  void testAuditEntryGenerationOnProjectStateUpdate() {
    // given
    WranglingState initialWranglingState = WranglingState.NEW;
    Project project = new Project("{\"name\": \"Project 1\"}");
    project.setWranglingState(initialWranglingState);
    this.mongoTemplate.save(project);

    // when
    WranglingState updatedWranglingState = WranglingState.ELIGIBLE;
    ObjectNode patchUpdate =
        new ObjectMapper()
            .createObjectNode()
            .put("wranglingState", updatedWranglingState.getValue());
    projectService.update(project, patchUpdate, false);

    // then
    AuditEntry actual = projectService.getProjectAuditEntries(project).get(0);

    assertThat(actual)
        .hasFieldOrPropertyWithValue("auditType", AuditType.STATUS_UPDATED)
        .hasFieldOrPropertyWithValue("before", initialWranglingState.name())
        .hasFieldOrPropertyWithValue("after", updatedWranglingState.name())
        .returns(true, e -> e.getUser().contains("Username: test_user;"));
  }

  @AfterEach
  private void tearDown() {
    this.mongoTemplate.dropCollection(Project.class);
    this.mongoTemplate.dropCollection(AuditEntry.class);
  }
}
