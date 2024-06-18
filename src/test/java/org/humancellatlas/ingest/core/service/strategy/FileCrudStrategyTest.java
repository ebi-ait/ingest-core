package org.humancellatlas.ingest.core.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.humancellatlas.ingest.core.service.strategy.impl.FileCrudStrategy;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {FileCrudStrategy.class})
public class FileCrudStrategyTest {
  @Autowired private FileCrudStrategy fileCrudStrategy;

  @MockBean private FileRepository fileRepository;
  @MockBean private ProjectRepository projectRepository;
  @MockBean private MessageRouter messageRouter;

  private File testFile;

  @BeforeEach
  void setUp() {
    testFile = new File(null, "fileName");
  }

  @Test
  public void testRemoveLinksFile() {
    // given
    Project projectWithFile = new Project(null);
    projectWithFile.getSupplementaryFiles().add(testFile);
    when(projectRepository.findBySupplementaryFilesContains(testFile))
        .thenReturn(Stream.of(projectWithFile));

    // when
    fileCrudStrategy.removeLinksToDocument(testFile);

    // then
    assertThat(projectWithFile.getSupplementaryFiles()).isEmpty();
    verify(projectRepository).save(projectWithFile);
  }

  @Test
  public void testDeleteFile() {
    // when
    fileCrudStrategy.deleteDocument(testFile);
    // then
    verify(fileRepository).delete(testFile);
  }
}
