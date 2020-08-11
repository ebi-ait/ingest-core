package org.humancellatlas.ingest.export.job;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.export.destination.ExportDestinationName;
import org.humancellatlas.ingest.export.job.web.ExportJobRequest;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<ExportJob> find(UUID submissionUuid,
                                ExportState exportState,
                                ExportDestinationName destinationName,
                                String version,
                                Pageable pageable) {
        SubmissionEnvelope submissionEnvelope = submissionEnvelopeRepository.findByUuidUuid(submissionUuid);
        ExportJob exportJobProbe = ExportJob.builder()
                                            .submission(submissionEnvelope)
                                            .status(exportState)
                                            .destination(new ExportDestination(destinationName, version, null))
                                            .build();
        return this.exportJobRepository.findAll(Example.of(exportJobProbe), pageable);
    }

}
