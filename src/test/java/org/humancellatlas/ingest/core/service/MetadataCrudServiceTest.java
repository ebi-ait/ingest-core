package org.humancellatlas.ingest.core.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;

import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.service.strategy.impl.*;
import org.humancellatlas.ingest.dataset.DatasetRepository;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.study.StudyRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

  @ParameterizedTest
  @MethodSource("providedTestDocuments")
  public void removeLinksSendsMessageToStateTracker(MetadataDocument document) {
    // when
    crudService.removeLinksToDocument(document);
    // then
    verify(messageRouter, times(1)).routeStateTrackingDeleteMessageFor(document);
  }

  @ParameterizedTest
  @MethodSource("providedTestDocuments")
  public void deleteSendsMessageToStateTracker(MetadataDocument document) {
    // when
    crudService.deleteDocument(document);
    // then
    verify(messageRouter, times(1)).routeStateTrackingDeleteMessageFor(document);
  }
}
