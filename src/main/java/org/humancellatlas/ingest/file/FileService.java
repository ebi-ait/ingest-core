package org.humancellatlas.ingest.file;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.core.Checksums;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.exception.CoreEntityNotFoundException;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.service.MetadataUpdateService;
import org.humancellatlas.ingest.file.web.FileMessage;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.state.MetadataDocumentEventHandler;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 06/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
public class FileService {
    private final @NonNull
    SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull
    FileRepository fileRepository;
    private final @NonNull
    BiomaterialRepository biomaterialRepository;
    private final @NonNull
    ProcessRepository processRepository;
    private final @NonNull
    MetadataDocumentEventHandler metadataDocumentEventHandler;
    private final @NonNull
    MetadataCrudService metadataCrudService;
    private final @NonNull
    MetadataUpdateService metadataUpdateService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public File addFileToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, File file) {
        if (!fileRepository.findBySubmissionEnvelopeAndFileName(submissionEnvelope, file.getFileName()).isEmpty()) {
            throw new FileAlreadyExistsException(String.format("File with name %s already exists in envelope %s", file.getFileName(), submissionEnvelope.getId()));
        } else {
            File createdFile = metadataCrudService.addToSubmissionEnvelopeAndSave(file, submissionEnvelope);
            metadataDocumentEventHandler.handleMetadataDocumentCreate(createdFile);
            return createdFile;
        }

    }

    public File addFileValidationJob(File file, ValidationJob validationJob) {
        if (file.getChecksums().getSha1().equals(validationJob.getChecksums().getSha1())) {
            file.setValidationJob(validationJob);
            return fileRepository.save(file);
        } else {
            throw new IllegalStateException(String.format("Failed to create validation job for file with ID %s : checksums mismatch", file.getId()));
        }
    }


    public void createFileFromFileMessage(FileMessage fileMessage) throws CoreEntityNotFoundException {
        String envelopeUuid = fileMessage.getStagingAreaId();
        SubmissionEnvelope envelope = findEnvelope(envelopeUuid);
        try {
            addFileToSubmissionEnvelope(envelope, new File(null, fileMessage.getFileName()));
        } catch (FileAlreadyExistsException e) {
            log.info(String.format("File listener attempted to create a File resource with name %s but it already existed for envelope %s",
                    fileMessage.getFileName(),
                    envelope.getId()));
        }

    }

    @Retryable(
            value = OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 500))
    public File updateFileFromFileMessage(FileMessage fileMessage) throws CoreEntityNotFoundException {
        String envelopeUuid = fileMessage.getStagingAreaId();
        SubmissionEnvelope envelope = findEnvelope(envelopeUuid);
        File updatedFile = findAndUpdateFile(fileMessage, envelope);
        return updatedFile;
    }

    private File findAndUpdateFile(FileMessage fileMessage, SubmissionEnvelope envelope) {
        String fileName = fileMessage.getFileName();
        File file = findFile(fileName, envelope);

        String newFileUrl = fileMessage.getCloudUrl();
        Checksums checksums = fileMessage.getChecksums();
        Long size = fileMessage.getSize();
        String contentType = fileMessage.getContentType();

        log.info(String.format("Updating file with cloudUrl %s and submission UUID %s", newFileUrl, envelope.getUuid()));

        file.setCloudUrl(newFileUrl);
        file.setChecksums(checksums);
        file.setSize(size);
        file.setFileContentType(contentType);
        file.enactStateTransition(ValidationState.DRAFT);
        File updatedFile = fileRepository.save(file);

        log.info(String.format("File validation state is %s for file with cloudUrl %s and submission UUID %s ", updatedFile.getValidationState(), file.getCloudUrl(), envelope.getUuid()));
        
        return updatedFile;
    }

    private SubmissionEnvelope findEnvelope(String envelopeUuid) throws CoreEntityNotFoundException {
        return Optional.ofNullable(submissionEnvelopeRepository.findByUuid(new Uuid(envelopeUuid)))
                .orElseThrow(() -> new CoreEntityNotFoundException(String.format("Couldn't find envelope with with uuid %s", envelopeUuid)));
    }

    private File findFile(String fileName, SubmissionEnvelope envelope) {
        List<File> filesInEnvelope = fileRepository.findBySubmissionEnvelopeAndFileName(envelope, fileName);

        if (filesInEnvelope.size() != 1) {
            throw new RuntimeException(String.format("Expected 1 file with name %s, but found %s", fileName, filesInEnvelope.size()));
        }
        return filesInEnvelope.get(0);
    }

}
