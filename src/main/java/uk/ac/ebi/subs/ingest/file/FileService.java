package uk.ac.ebi.subs.ingest.file;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.biomaterial.BiomaterialRepository;
import uk.ac.ebi.subs.ingest.core.Checksums;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.core.exception.CoreEntityNotFoundException;
import uk.ac.ebi.subs.ingest.core.service.MetadataCrudService;
import uk.ac.ebi.subs.ingest.core.service.MetadataUpdateService;
import uk.ac.ebi.subs.ingest.file.web.FileMessage;
import uk.ac.ebi.subs.ingest.process.ProcessRepository;
import uk.ac.ebi.subs.ingest.project.ProjectRepository;
import uk.ac.ebi.subs.ingest.state.MetadataDocumentEventHandler;
import uk.ac.ebi.subs.ingest.state.ValidationState;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
@Validated
public class FileService {
  private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
  private final @NonNull FileRepository fileRepository;
  private final @NonNull BiomaterialRepository biomaterialRepository;
  private final @NonNull ProcessRepository processRepository;
  private final @NonNull ProjectRepository projectRepository;
  private final @NonNull MetadataDocumentEventHandler metadataDocumentEventHandler;
  private final @NonNull MetadataCrudService metadataCrudService;
  private final @NonNull MetadataUpdateService metadataUpdateService;

  private final Logger log = LoggerFactory.getLogger(getClass());

  public File addFileToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, @Valid File file) {
    if (!fileRepository
        .findBySubmissionEnvelopeAndFileName(submissionEnvelope, file.getFileName())
        .isEmpty()) {
      throw new FileAlreadyExistsException(
          String.format(
              "File with name %s already exists in envelope %s",
              file.getFileName(), submissionEnvelope.getId()));
    } else {
      projectRepository
          .findBySubmissionEnvelopesContains(submissionEnvelope)
          .findFirst()
          .ifPresent(file::setProject);
      File createdFile =
          metadataCrudService.addToSubmissionEnvelopeAndSave(file, submissionEnvelope);
      metadataDocumentEventHandler.handleMetadataDocumentCreate(createdFile);
      return createdFile;
    }
  }

  public File addFileValidationJob(File file, ValidationJob validationJob) {
    if (file.getChecksums().getSha1().equals(validationJob.getChecksums().getSha1())) {
      file.setValidationJob(validationJob);
      return fileRepository.save(file);
    } else {
      throw new IllegalStateException(
          String.format(
              "Failed to create validation job for file with ID %s : checksums mismatch",
              file.getId()));
    }
  }

  public void createFileFromFileMessage(FileMessage fileMessage)
      throws CoreEntityNotFoundException {
    String envelopeUuid = fileMessage.getStagingAreaId();
    SubmissionEnvelope envelope = findEnvelope(envelopeUuid);
    try {
      addFileToSubmissionEnvelope(envelope, new File(null, fileMessage.getFileName()));
    } catch (FileAlreadyExistsException e) {
      log.info(
          String.format(
              "File listener attempted to create a File resource with name %s but it already existed for envelope %s",
              fileMessage.getFileName(), envelope.getId()));
    }
  }

  @Retryable(
      value = OptimisticLockingFailureException.class,
      maxAttempts = 5,
      backoff = @Backoff(delay = 500, maxDelay = 60000, multiplier = 2))
  public File updateFileFromFileMessage(FileMessage fileMessage)
      throws CoreEntityNotFoundException {
    String envelopeUuid = fileMessage.getStagingAreaId();
    SubmissionEnvelope envelope = findEnvelope(envelopeUuid);
    return findAndUpdateFile(fileMessage, envelope);
  }

  private File findAndUpdateFile(FileMessage fileMessage, SubmissionEnvelope envelope) {
    String fileName = fileMessage.getFileName();
    File file = findFile(fileName, envelope);

    String newFileUrl = fileMessage.getCloudUrl();
    Checksums checksums = fileMessage.getChecksums();
    Long size = fileMessage.getSize();
    String contentType = fileMessage.getContentType();

    log.info(
        String.format(
            "Updating file with cloudUrl %s and submission UUID %s",
            newFileUrl, envelope.getUuid()));

    file.setCloudUrl(newFileUrl);
    file.setChecksums(checksums);
    file.setSize(size);
    file.setFileContentType(contentType);
    file.enactStateTransition(ValidationState.DRAFT);
    File updatedFile = fileRepository.save(file);

    log.info(
        String.format(
            "File validation state is %s for file with cloudUrl %s and submission UUID %s ",
            updatedFile.getValidationState(), file.getCloudUrl(), envelope.getUuid()));

    return updatedFile;
  }

  private SubmissionEnvelope findEnvelope(String envelopeUuid) throws CoreEntityNotFoundException {
    return Optional.ofNullable(submissionEnvelopeRepository.findByUuid(new Uuid(envelopeUuid)))
        .orElseThrow(
            () ->
                new CoreEntityNotFoundException(
                    String.format("Couldn't find envelope with with uuid %s", envelopeUuid)));
  }

  private File findFile(String fileName, SubmissionEnvelope envelope) {
    List<File> filesInEnvelope =
        fileRepository.findBySubmissionEnvelopeAndFileName(envelope, fileName);

    if (filesInEnvelope.size() != 1) {
      throw new RuntimeException(
          String.format(
              "Expected 1 file with name %s, but found %s", fileName, filesInEnvelope.size()));
    }
    return filesInEnvelope.get(0);
  }
}
