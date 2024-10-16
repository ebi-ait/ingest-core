package uk.ac.ebi.subs.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.ac.ebi.subs.ingest.audit.AuditEntry;
import uk.ac.ebi.subs.ingest.audit.AuditEntryRepository;
import uk.ac.ebi.subs.ingest.audit.AuditEntryService;
import uk.ac.ebi.subs.ingest.audit.AuditType;
import uk.ac.ebi.subs.ingest.bundle.BundleManifestRepository;
import uk.ac.ebi.subs.ingest.config.MigrationConfiguration;
import uk.ac.ebi.subs.ingest.core.service.MetadataCrudService;
import uk.ac.ebi.subs.ingest.core.service.MetadataUpdateService;
import uk.ac.ebi.subs.ingest.project.*;
import uk.ac.ebi.subs.ingest.schemas.SchemaService;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

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
