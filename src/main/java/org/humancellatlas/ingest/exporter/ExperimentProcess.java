package org.humancellatlas.ingest.exporter;

import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.messaging.model.ExportEntityMessage;
import org.humancellatlas.ingest.messaging.model.ManifestMessage;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.project.Project;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;


public class ExperimentProcess {

    private final int index;
    private final int totalCount;

    private final Process process;

    private final SubmissionEnvelope submissionEnvelope;

    private final Project project;

    public ExperimentProcess(int index, int totalCount, Process process, SubmissionEnvelope submission, Project project) {
        this.index = index;
        this.totalCount = totalCount;
        this.process = process;
        this.submissionEnvelope = submission;
        this.project = project;
    }

    public Integer getIndex() {
        return index;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public Process getProcess() {
        return process;
    }

    public SubmissionEnvelope getSubmissionEnvelope() {
        return submissionEnvelope;
    }

    public ExportEntityMessage toExportEntityMessage(LinkGenerator linkGenerator, ExportJob exportJob, Map<String, Object> context) {
        String callbackLink = linkGenerator.createCallback(process.getClass(), process.getId());
        return new ExportEntityMessage(
                exportJob.getId(),
                process.getId(),
                process.getUuid().toString(),
                callbackLink,
                process.getClass().getSimpleName().toLowerCase(),
                submissionEnvelope.getId(),
                submissionEnvelope.getUuid().toString(),
                project.getId(),
                process.getProject().getUuid().toString(),
                index,
                totalCount,
                context);
    }

    public ManifestMessage toManifestMessage(LinkGenerator linkGenerator) {
        String callbackLink = linkGenerator.createCallback(process.getClass(), process.getId());
        return new ManifestMessage(
                UUID.randomUUID(),
                Instant.now().toString(),
                process.getId(),
                process.getUuid().toString(),
                callbackLink,
                process.getClass().getSimpleName(),
                submissionEnvelope.getId(),
                submissionEnvelope.getUuid().toString(),
                index,
                totalCount);

    }
}
