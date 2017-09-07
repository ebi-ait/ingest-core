package org.humancellatlas.ingest.file;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.envelope.SubmissionEnvelope;
import org.humancellatlas.ingest.envelope.SubmissionEnvelopeRepository;
import org.springframework.stereotype.Service;

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
}
