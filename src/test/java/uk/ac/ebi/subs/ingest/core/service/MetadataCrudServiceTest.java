package uk.ac.ebi.subs.ingest.core.service;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.ac.ebi.subs.ingest.biomaterial.Biomaterial;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.core.service.strategy.impl.*;
import uk.ac.ebi.subs.ingest.dataset.DatasetRepository;
import uk.ac.ebi.subs.ingest.file.File;
import uk.ac.ebi.subs.ingest.file.FileRepository;
import uk.ac.ebi.subs.ingest.messaging.MessageRouter;
import uk.ac.ebi.subs.ingest.process.Process;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.protocol.Protocol;
import uk.ac.ebi.subs.ingest.protocol.ProtocolRepository;
import uk.ac.ebi.subs.ingest.study.StudyRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {
      MetadataCrudService.class,
      BiomaterialCrudStrategy.class,
      FileCrudStrategy.class,
      ProcessCrudStrategy.class,
      ProjectCrudStrategy.class,
      ProtocolCrudStrategy.class,
      StudyCrudStrategy.class,
      DatasetCrudStrategy.class
    })
public class MetadataCrudServiceTest {
  @Autowired private MetadataCrudService crudService;
  @Autowired private BiomaterialCrudStrategy biomaterialCrudStrategy;
  @Autowired private FileCrudStrategy fileCrudStrategy;
  @Autowired private ProcessCrudStrategy processCrudStrategy;
  @Autowired private ProjectCrudStrategy projectCrudStrategy;
  @Autowired private ProtocolCrudStrategy protocolCrudStrategy;
  @Autowired private StudyCrudStrategy studyCrudStrategy;
  @Autowired private DatasetCrudStrategy datasetCrudStrategy;

  @MockBean private MessageRouter messageRouter;
  @MockBean private BiomaterialRepository biomaterialRepository;
  @MockBean private FileRepository fileRepository;
  @MockBean private ProcessRepository processRepository;
  @MockBean private ProjectRepository projectRepository;
  @MockBean private ProtocolRepository protocolRepository;
  @MockBean private StudyRepository studyRepository;
  @MockBean private DatasetRepository datasetRepository;

  private static Stream<Arguments> providedTestDocuments() {
    return Stream.of(
        Arguments.of(new Biomaterial(null)),
        Arguments.of(new File(null, "fileName")),
        Arguments.of(new Process(null)),
        Arguments.of(new Project(null)),
        Arguments.of(new Protocol(null)));
  }
}
