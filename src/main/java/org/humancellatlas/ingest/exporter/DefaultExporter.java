package org.humancellatlas.ingest.exporter;

import org.apache.commons.collections4.ListUtils;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.export.job.ExportJobRepository;
import org.humancellatlas.ingest.export.job.web.ExportJobRequest;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.humancellatlas.ingest.export.destination.ExportDestinationName.DCP;

@Component
public class DefaultExporter implements Exporter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private ExportJobRepository exportJobRepository;

    @Autowired
    private MessageRouter messageRouter;

    /**
     * Divides a set of process IDs into lists of size partitionSize
     *
     * @param processIds
     * @param partitionSize
     * @return A collection of partitionSize sized lists of processes
     */
    private static List<List<String>> partitionProcessIds(Collection<String> processIds, int partitionSize) {
        return ListUtils.partition(new ArrayList<>(processIds), partitionSize);
    }

    @Override
    public void exportManifests(SubmissionEnvelope envelope) {
        Collection<String> assayingProcessIds = processService.findAssays(envelope);

        log.info(String.format("Found %s assays processes for envelope with ID %s",
                assayingProcessIds.size(),
                envelope.getId()));

        int totalCount = assayingProcessIds.size();
        ExperimentProcess.IndexCounter counter = new ExperimentProcess.IndexCounter(totalCount);

        int partitionSize = 500;
        partitionProcessIds(assayingProcessIds, partitionSize)
                .stream()
                .flatMap(processIdBatch -> processService.getProcesses(processIdBatch))
                .map(process -> ExperimentProcess.from(process, counter))
                .forEach(messageRouter::sendManifestForExport);
    }

    @Override
    public void exportData(SubmissionEnvelope envelope, Project project) {
        var destinationContext = new JSONObject();
        destinationContext.put("projectUuid", project.getUuid().getUuid().toString());

        var exportJobContext = new JSONObject();
        exportJobContext.put("dataFileTransfer", false);
        ExportJob exportJob = createDcpExportJob(envelope, destinationContext, exportJobContext);

        var messageContext = new JSONObject();
        messageRouter.sendSubmissionForDataExport(exportJob, messageContext);
    }

    @Override
    public void generateSpreadsheet(SubmissionEnvelope submissionEnvelope) {
        var exportJob = createDcpExportJob(submissionEnvelope, new JSONObject(), new JSONObject());
        generateSpreadsheet(exportJob);
    }

    @Override
    public void exportMetadata(ExportJob exportJob) {
        var submission = exportJob.getSubmission();
        Collection<String> assayingProcessIds = processService.findAssays(submission);
        exportJob.getContext().put("totalAssayCount", assayingProcessIds.size());
        exportJobRepository.save(exportJob);
        updateDcpVersionAndSendMessageForEachProcess(assayingProcessIds, exportJob);
    }

    private ExportJob createDcpExportJob(SubmissionEnvelope submissionEnvelope, JSONObject destinationContext, JSONObject exportJobContext) {
        ExportDestination exportDestination = new ExportDestination(DCP, "v2", destinationContext);
        ExportJobRequest exportJobRequest = new ExportJobRequest(exportDestination, exportJobContext);
        ExportJob newExportJob = ExportJob.builder()
            .submission(submissionEnvelope)
            .destination(exportJobRequest.getDestination())
            .context(exportJobRequest.getContext())
            .build();
        return exportJobRepository.insert(newExportJob);
    }

    private void updateDcpVersionAndSendMessageForEachProcess(Collection<String> assayingProcessIds, ExportJob exportJob) {
        int totalCount = assayingProcessIds.size();
        ExperimentProcess.IndexCounter counter = new ExperimentProcess.IndexCounter(totalCount);

        int partitionSize = 500;
        partitionProcessIds(assayingProcessIds, partitionSize)
                .stream()
                .flatMap(processIdBatch -> processService.getProcesses(processIdBatch))
                .map(process -> (Process) process.setDcpVersion(exportJob.getCreatedDate()))
                .map(process -> processRepository.save(process))
                .map(process -> ExperimentProcess.from(process, counter))
                .forEach(exportData -> messageRouter.sendExperimentForExport(exportData, exportJob, null));
    }

    @Override
    public void generateSpreadsheet(ExportJob exportJob) {
        exportJob.getContext().put("spreadsheetGeneration", false);
        exportJobRepository.save(exportJob);
        var messageContext = new JSONObject();
        messageRouter.sendGenerateSpreadsheet(exportJob, messageContext);
    }
}
