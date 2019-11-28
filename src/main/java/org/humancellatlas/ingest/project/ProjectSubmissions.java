package org.humancellatlas.ingest.project;

import lombok.Data;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectSubmissions {
    private String projectId;
    private Uuid projectUuid;
    private List<String> submissions;
    private List<String> missingSubmissions;

    public ProjectSubmissions(Project project) {
        projectId = project.getId();
        projectUuid = project.getUuid();
        submissions = new ArrayList<>();
        addLinkedSubmissions(project.getSubmissionEnvelopes());
        missingSubmissions = new ArrayList<>();
    }

    private void addLinkedSubmissions(Iterable<SubmissionEnvelope> envelopes) {
        for (SubmissionEnvelope env : envelopes)
            submissions.add(env.getId());
    }

    public void addSubmissions(Iterable<SubmissionEnvelope> envelopes) {
        for (SubmissionEnvelope envelope : envelopes)
            if (!submissions.contains(envelope.getId()) && !missingSubmissions.contains(envelope.getId()) )
                missingSubmissions.add(envelope.getId());
    }

    public boolean HasMissingSubmissions() {
        return !missingSubmissions.isEmpty();
    }
}
