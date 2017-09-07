package org.humancellatlas.ingest.file;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.exception.CoreEntityNotFoundException;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.envelope.SubmissionEnvelopeRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
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
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull FileRepository fileRepository;

    public File addFileToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, File file) {
        File result = getFileRepository().save(file);
        getSubmissionEnvelopeRepository().save(submissionEnvelope.addFile(result));
        return result;
    }

    public File updateStagedFileUrl(UUID envelopeUuid, String fileName, String newFileUrl) throws CoreEntityNotFoundException {
        Optional<SubmissionEnvelope> envelope = Optional.ofNullable(submissionEnvelopeRepository.findByUuid(envelopeUuid));

        if(envelope.isPresent()) {
            List<File> filesInEnvelope = submissionEnvelopeRepository.findByUuid(envelopeUuid).getFiles();

            Optional<File> fileToUpdate = filesInEnvelope.stream()
                    .filter(file -> file.getFileName().equals(fileName))
                    .findFirst();

            if(fileToUpdate.isPresent()){
                File file = fileToUpdate.get();
                file.setCloudUrl(newFileUrl);
                File updatedFile = fileRepository.save(file);
                return updatedFile;
            } else {
                // todo log
                throw new CoreEntityNotFoundException(String.format("Couldn't find file with name %s in envelope with uuid %s", fileName, envelopeUuid));
            }
        } else {
            // todo log
            throw new CoreEntityNotFoundException(String.format("Couldn't find envelope with with uuid %s", envelopeUuid));
        }

    }
}
