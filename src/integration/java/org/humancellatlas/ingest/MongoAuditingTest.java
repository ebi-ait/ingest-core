package org.humancellatlas.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.humancellatlas.ingest.config.MigrationConfiguration;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
public class MongoAuditingTest {

  @MockBean private MigrationConfiguration migrationConfiguration;

  @Autowired private ProjectRepository projectRepository;

  @Test
  @WithMockUser("johndoe")
  public void auditMongoRecord() {
    // given:
    Project project = new Project(new HashMap<>());

    // when:
    Project persistentProject = projectRepository.save(project);

    // then:
    /* NOTE there doesn't seem to be a clean and easy way to check this without updating the UserAuditing class
    itself to assume that the default principal type contains username and password. */
    assertThat(persistentProject.getUser()).contains("johndoe");
  }
}
