package org.humancellatlas.ingest.project;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.stereotype.Service;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@Service
@RequiredArgsConstructor
@Getter
public class ProjectService {
    private final @NonNull SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final @NonNull ProjectRepository projectRepository;

    public Project addProjectToSubmissionEnvelope(SubmissionEnvelope submissionEnvelope, Project project) {
        project.addToSubmissionEnvelope(submissionEnvelope);
        return getProjectRepository().save(project);
    }
}
