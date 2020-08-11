package org.humancellatlas.ingest.export.job;

import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.job.web.ExportJobRequest;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@AllArgsConstructor
public class ExportJobService {
    private final ExportJobRepository exportJobRepository;

    public ExportJob createExportJob(SubmissionEnvelope submissionEnvelope, ExportJobRequest exportJobRequest) {
        ExportJob newExportJob = ExportJob.builder()
            .status(ExportState.Exporting)
            .errors(new ArrayList<>())
            .context(new Object())
            .submission(submissionEnvelope)
            .destination(exportJobRequest.getDestination())
            .context(exportJobRequest.getContext())
            .build();
        return exportJobRepository.insert(newExportJob);
    }

}
