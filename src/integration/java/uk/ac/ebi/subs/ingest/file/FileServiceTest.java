package uk.ac.ebi.subs.ingest.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.OptimisticLockingFailureException;

import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.config.MigrationConfiguration;
import uk.ac.ebi.subs.ingest.core.Checksums;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.core.exception.CoreEntityNotFoundException;
import uk.ac.ebi.subs.ingest.core.service.MetadataCrudService;
import uk.ac.ebi.subs.ingest.core.service.MetadataUpdateService;
import uk.ac.ebi.subs.ingest.file.web.FileMessage;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.project.Project;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.state.MetadataDocumentEventHandler;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

@SpringBootTest
public class FileServiceTest {
  @MockBean MigrationConfiguration migrationConfiguration;

  @MockBean FileRepository fileRepository;

  @MockBean SubmissionEnvelopeRepository submissionEnvelopeRepository;

  @MockBean BiomaterialRepository biomaterialRepository;

  @MockBean ProcessRepository processRepository;

  @MockBean ProjectRepository projectRepository;

  @MockBean MetadataDocumentEventHandler metadataDocumentEventHandler;

  @MockBean MetadataCrudService metadataCrudService;

  @MockBean MetadataUpdateService metadataUpdateService;

  @Autowired FileService fileService;

  @Autowired private ApplicationContext applicationContext;

  FileMessage fileMessage;

  SubmissionEnvelope submissionEnvelope;

  File file;

  Project project;

  @BeforeEach
  void setUp() {
    applicationContext.getBeansWithAnnotation(MockBean.class).forEach(Mockito::reset);

    Checksums checksums = new Checksums("sha1", "sha256", "crc32c", "s3Etag");
    String submissionUuid = UUID.randomUUID().toString();
    String filename = "filename";
    fileMessage =
        new FileMessage("cloudUrl", filename, submissionUuid, "content_type", checksums, 123);

    submissionEnvelope = new SubmissionEnvelope();

    project = spy(new Project(null));
    when(project.getId()).thenReturn("projectId");

    file = new File(null, filename);
    var files = new ArrayList<File>();
    files.add(file);

    when(submissionEnvelopeRepository.findByUuid(any(Uuid.class))).thenReturn(submissionEnvelope);
    when(projectRepository.findBySubmissionEnvelopesContains(submissionEnvelope))
        .thenReturn(Stream.of(project));
    when(fileRepository.findBySubmissionEnvelopeAndFileName(
            submissionEnvelope, fileMessage.getFileName()))
        .thenReturn(files);
    when(fileRepository.save(file)).thenReturn(file);
  }

  @Test
  public void testCreateFileFromFileMessage() throws CoreEntityNotFoundException {
    // given:
    when(fileRepository.findBySubmissionEnvelopeAndFileName(
            submissionEnvelope, fileMessage.getFileName()))
        .thenReturn(new ArrayList<>());

    // when:
    fileService.createFileFromFileMessage(fileMessage);

    // then:
    verify(metadataCrudService)
        .addToSubmissionEnvelopeAndSave(any(File.class), any(SubmissionEnvelope.class));
  }

  @Test
  public void testCreateFileFromFileMessageNotCreated() throws CoreEntityNotFoundException {
    // when:
    fileService.createFileFromFileMessage(fileMessage);

    // then:
    verify(metadataCrudService, never())
        .addToSubmissionEnvelopeAndSave(any(File.class), any(SubmissionEnvelope.class));
  }

  @Test
  public void testUpdateFileFromFileMessage() throws CoreEntityNotFoundException {
    // when:
    fileService.updateFileFromFileMessage(fileMessage);

    // then:
    verify(fileRepository).save(file);
    assertThat(file.getCloudUrl()).isEqualTo(fileMessage.getCloudUrl());
    assertThat(file.getChecksums()).isEqualTo(fileMessage.getChecksums());
    assertThat(file.getFileContentType()).isEqualTo(fileMessage.getContentType());
    assertThat(file.getValidationState()).isEqualTo(ValidationState.DRAFT);
  }

  @Test
  public void testUpdateFileFromFileMessageRetry() throws CoreEntityNotFoundException {
    // given:
    when(fileRepository.save(file))
        .thenThrow(new OptimisticLockingFailureException("Error"))
        .thenReturn(file);

    // when:
    fileService.updateFileFromFileMessage(fileMessage);

    // then:
    verify(fileRepository, times(2)).save(file);
  }

  @Test
  public void testUpdateFileFromFileMessageMaxRetries() throws CoreEntityNotFoundException {
    // given:
    when(fileRepository.save(file)).thenThrow(new OptimisticLockingFailureException("Error"));

    // when:
    assertThatExceptionOfType(OptimisticLockingFailureException.class)
        .isThrownBy(
            () -> {
              fileService.updateFileFromFileMessage(fileMessage);
            });

    // then:
    verify(fileRepository, times(5)).save(file);
  }
}
