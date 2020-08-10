package org.humancellatlas.ingest.export.job;

import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.export.job.web.ExportJobRequest;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExportJobService {
    private final ExportJobRepository exportJobRepository;
    private final SubmissionEnvelopeRepository submissionEnvelopeRepository;

    public ExportJob createExportJob(SubmissionEnvelope submissionEnvelope, ExportJobRequest exportJobRequest) {
        ExportJob newExportJob = ExportJob.buildNew()
            .submission(submissionEnvelope)
            .destination(exportJobRequest.getDestination())
            .context(exportJobRequest.getContext())
            .build();
        return exportJobRepository.insert(newExportJob);
    }

}
