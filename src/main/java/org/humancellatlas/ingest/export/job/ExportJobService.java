package org.humancellatlas.ingest.export.job;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.export.destination.ExportDestinationName;
import org.humancellatlas.ingest.export.job.web.ExportJobRequest;
import org.humancellatlas.ingest.exporter.Exporter;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.SubmissionEnvelopeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@AllArgsConstructor
public class ExportJobService {
    private final ExportJobRepository exportJobRepository;
    private final SubmissionEnvelopeRepository submissionEnvelopeRepository;
    private final Exporter exporter;
    private final @NonNull ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final @NonNull Logger log = LoggerFactory.getLogger(getClass());

    public ExportJob createExportJob(SubmissionEnvelope submissionEnvelope, ExportJobRequest exportJobRequest) {
        ExportJob newExportJob = ExportJob.builder()
                .status(ExportState.EXPORTING)
                .errors(new ArrayList<>())
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

    public ExportJob updateTransferStatus(ExportJob exportJob, String transferStatus) {
        exportJob.getContext().put("dataFileTransfer", transferStatus);
        var savedJob = exportJobRepository.save(exportJob);
        if (Objects.equals(transferStatus, "COMPLETE")) {
            executorService.submit(() -> {
                try {
                    exporter.exportMetadata(exportJob);
                } catch (Exception e) {
                    log.error("Uncaught Exception sending message to export Metadata for Export Job", e);
                }
            });
        }
        return savedJob;
    }
}
