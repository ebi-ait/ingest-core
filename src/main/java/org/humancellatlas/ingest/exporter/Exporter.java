package org.humancellatlas.ingest.exporter;

import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;

public interface Exporter {

    void exportManifests(SubmissionEnvelope submissionEnvelope);

    void exportMetadata(ExportJob exportJob);

    void exportMetadata(SubmissionEnvelope envelope);

    void exportData(SubmissionEnvelope submissionEnvelope, Project project);

    void generateSpreadsheet(ExportJob exportJob);
}
