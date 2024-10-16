package uk.ac.ebi.subs.ingest.export.job;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import uk.ac.ebi.subs.ingest.export.ExportState;
import uk.ac.ebi.subs.ingest.export.destination.ExportDestination;
import uk.ac.ebi.subs.ingest.export.destination.ExportDestinationName;
import uk.ac.ebi.subs.ingest.export.job.web.ExportJobRequest;
import uk.ac.ebi.subs.ingest.exporter.Exporter;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelopeRepository;

@Component
@AllArgsConstructor
public class ExportJobService {
  private final ExportJobRepository exportJobRepository;
  private final SubmissionEnvelopeRepository submissionEnvelopeRepository;
  private final Exporter exporter;
  private final @NonNull ExecutorService executorService = Executors.newFixedThreadPool(5);
  private final @NonNull Logger log = LoggerFactory.getLogger(getClass());

  public ExportJob createExportJob(
      SubmissionEnvelope submissionEnvelope, ExportJobRequest exportJobRequest) {
    ExportJob newExportJob =
        ExportJob.builder()
            .submission(submissionEnvelope)
            .destination(exportJobRequest.getDestination())
            .context(exportJobRequest.getContext())
            .build();
    return exportJobRepository.insert(newExportJob);
  }

  public Page<ExportJob> find(
      UUID submissionUuid,
      ExportState exportState,
      ExportDestinationName destinationName,
      String version,
      Pageable pageable) {
    SubmissionEnvelope submissionEnvelope =
        submissionEnvelopeRepository.findByUuidUuid(submissionUuid);
    ExportJob exportJobProbe =
        ExportJob.builder()
            .submission(submissionEnvelope)
            .status(exportState)
            .destination(new ExportDestination(destinationName, version, null))
            .build();
    return this.exportJobRepository.findAll(Example.of(exportJobProbe), pageable);
  }

  public ExportJob updateContext(ExportJob exportJob, Map<String, Object> context) {
    exportJob.getContext().putAll(context);
    var savedJob = exportJobRepository.save(exportJob);
    if (context.getOrDefault("dataFileTransfer", "").equals("COMPLETE")) {
      submit(exporter::generateSpreadsheet, exportJob, "spreadsheetGeneration");
    } else if (context.getOrDefault("spreadsheetGeneration", "").equals("COMPLETE")) {
      submit(exporter::exportMetadata, exportJob, "exportMetadata");
    }
    return savedJob;
  }

  private void submit(Consumer<ExportJob> exportAction, ExportJob exportJob, String actionName) {
    String submissionUuid = exportJob.getSubmission().getUuid().getUuid().toString();
    executorService.submit(
        () -> {
          try {
            log.info(
                "submitting export action {} for export job {} for submission {}",
                actionName,
                exportJob.getId(),
                submissionUuid);
            exportAction.accept(exportJob);
          } catch (Exception e) {
            log.error(
                String.format(
                    "Uncaught Exception sending message %s for Export Job %s for submission %s",
                    actionName, exportJob.getId(), submissionUuid),
                e);
          }
        });
  }
}
