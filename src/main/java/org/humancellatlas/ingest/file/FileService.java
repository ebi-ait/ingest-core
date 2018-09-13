package org.humancellatlas.ingest.file;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.exception.CoreEntityNotFoundException;
import org.humancellatlas.ingest.state.MetadataDocumentEventHandler;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull FileRepository fileRepository;
    private final @NonNull MetadataDocumentEventHandler metadataDocumentEventHandler;

    // TODO Refactor!!!
    public File createFile(String fileName, File file, SubmissionEnvelope submissionEnvelope) {
        if(! fileRepository.findBySubmissionEnvelopesInAndFileName(submissionEnvelope, fileName).isEmpty()) {
            throw new FileAlreadyExistsException(String.format("File with name %s already exists in envelope %s", fileName, submissionEnvelope.getId()),
                                                 fileName);
        } else {
            file.setFileName(fileName);
            file.addToSubmissionEnvelope(submissionEnvelope);
            File createdFile = fileRepository.save(file);
            metadataDocumentEventHandler.handleMetadataDocumentCreate(createdFile);
            return createdFile;
        }
    }

    public File addFileToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, File file) {
        file.addToSubmissionEnvelope(submissionEnvelope);
        File createdFile = getFileRepository().save(file);
        metadataDocumentEventHandler.handleMetadataDocumentCreate(createdFile);
        return createdFile;
    }

    public File updateStagedFileUrl(String envelopeUuid, String fileName, String newFileUrl) throws CoreEntityNotFoundException {
        Optional<SubmissionEnvelope> envelope = Optional.ofNullable(submissionEnvelopeRepository.findByUuid(new Uuid(envelopeUuid)));

        if(envelope.isPresent()) {
            List<File> filesInEnvelope = fileRepository.findBySubmissionEnvelopesInAndFileName(envelope.get(), fileName);

            if(filesInEnvelope.size() != 1) {
                throw new RuntimeException(String.format("Expected 1 file with name %s, but found %s", fileName, filesInEnvelope.size()));
            } else {
                File file = filesInEnvelope.get(0);
                file.setCloudUrl(newFileUrl);
                if(!file.getValidationState().equals(ValidationState.DRAFT)){
                    file.enactStateTransition(ValidationState.DRAFT);
                }
                File updatedFile = fileRepository.save(file);
                return updatedFile;
            }
        } else {
            // todo log
            throw new CoreEntityNotFoundException(String.format("Couldn't find envelope with with uuid %s", envelopeUuid));
        }

    }
}
