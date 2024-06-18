package org.humancellatlas.ingest.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.state.MetadataDocumentEventHandler;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ProcessServiceTest {

  @Autowired private ProcessService service;

  @MockBean private SubmissionEnvelopeRepository submissionEnvelopeRepository;
  @MockBean private ProcessRepository processRepository;
  @MockBean private FileRepository fileRepository;
  @MockBean private BiomaterialRepository biomaterialRepository;
  @MockBean private BundleManifestRepository bundleManifestRepository;
  @MockBean private ProjectRepository projectRepository;
  @MockBean private MetadataDocumentEventHandler metadataDocumentEventHandler;
  @MockBean private MetadataCrudService metadataCrudService;
  @MockBean private MetadataUpdateService metadataUpdateService;

  String fileName = "ERR1630013.fastq.gz";
  File file;
  Process analysis;
  SubmissionEnvelope submissionEnvelope;

  @BeforeEach
  void setUp() {
    // Given:
    file = spy(new File(null, fileName));
    analysis = new Process(null);
    submissionEnvelope = new SubmissionEnvelope();
    analysis.setSubmissionEnvelope(submissionEnvelope);
  }

  @Test
  public void testAddFileToAnalysisProcess() {
    // given:
    doReturn(Collections.emptyList())
        .when(fileRepository)
        .findBySubmissionEnvelopeAndFileName(any(SubmissionEnvelope.class), anyString());

    // when:
    Process result = service.addOutputFileToAnalysisProcess(analysis, file);

    // then:
    assertThat(result).isEqualTo(analysis);
    verify(file).addToAnalysis(analysis);
    verify(fileRepository).save(file);
  }

  @Test
  public void testAddFileToAnalysisProcessWhenFileAlreadyExists() {
    // given:
    List<File> persistentFiles = Arrays.asList(file);
    doReturn(persistentFiles)
        .when(fileRepository)
        .findBySubmissionEnvelopeAndFileName(submissionEnvelope, fileName);

    // when:
    Process result = service.addOutputFileToAnalysisProcess(analysis, file);

    // then:
    assertThat(result).isEqualTo(analysis);

    // and:
    verify(file).addToAnalysis(analysis);
    verify(fileRepository).save(file);
  }

  @Configuration
  static class TestConfiguration {

    @Bean
    ProcessService processService() {
      return new ProcessService();
    }
  }
}
