package org.humancellatlas.ingest.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ProjectLinkChangeListener.class)
class ProjectLinkChangeListenerTest {
  @MockBean ProjectService projectService;
  @Autowired ProjectLinkChangeListener projectLinkChangeListener;

  @Test
  void test_whenUpdatingStatus_usingProjectService() {
    // given
    Project project = new Project(null);
    project.setUuid(Uuid.newUuid());
    SubmissionEnvelope submissionEnvelope = new SubmissionEnvelope();
    project.addToSubmissionEnvelopes(submissionEnvelope);
    List<SubmissionEnvelope> submissions = List.of(submissionEnvelope);

    // then
    projectLinkChangeListener.beforeLinkSave(project, submissions);

    // then
    verify(projectService).updateWranglingState(eq(project), any());
  }
}
