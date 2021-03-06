package org.humancellatlas.ingest.exporter;

import org.apache.commons.collections4.ListUtils;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.export.ExportState;
import org.humancellatlas.ingest.export.destination.ExportDestination;
import org.humancellatlas.ingest.export.entity.ExportEntityService;
import org.humancellatlas.ingest.export.entity.web.ExportEntityRequest;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.humancellatlas.ingest.export.job.ExportJobService;
import org.humancellatlas.ingest.export.job.web.ExportJobRequest;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static org.humancellatlas.ingest.export.destination.ExportDestinationName.DCP;

@Component
public class DefaultExporter implements Exporter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ExportJobService exportJobService;

    @Autowired
    private ExportEntityService exportEntityService;

    @Autowired
    private MessageRouter messageRouter;

    @Autowired
    private LinkGenerator linkGenerator;

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

        IndexCounter counter = new IndexCounter();
        int totalCount = assayingProcessIds.size();

        int partitionSize = 500;
        partitionProcessIds(assayingProcessIds, partitionSize)
                .stream()
                .map(processIdBatch -> processService.getProcesses(processIdBatch))
                .flatMap(Function.identity())
                .map(process -> new ExperimentProcess(counter.next(), totalCount, process))
                .forEach(messageRouter::sendManifestForExport);
    }

    @Override
    public void exportProcesses(SubmissionEnvelope envelope) {
        Collection<String> assayingProcessIds = processService.findAssays(envelope);

        log.info(String.format("Found %s assays processes for envelope with ID %s",
                assayingProcessIds.size(),
                envelope.getId()));

        IndexCounter counter = new IndexCounter();
        int totalCount = assayingProcessIds.size();

        int partitionSize = 500;

        JSONObject context = new JSONObject();
        context.put("totalAssayCount", totalCount);

        ExportDestination exportDestination = new ExportDestination(DCP, "v2", null);

        ExportJobRequest exportJobRequest = new ExportJobRequest(exportDestination, context);

        ExportJob exportJob = exportJobService.createExportJob(envelope, exportJobRequest);

        partitionProcessIds(assayingProcessIds, partitionSize)
                .stream()
                .map(processIdBatch -> processService.getProcesses(processIdBatch))
                .flatMap(Function.identity())
                .map(process -> new ExperimentProcess(counter.next(), totalCount, process))
                .forEach(exportData -> {
                    messageRouter.sendExperimentForExport(exportData, exportJob);
                });

    }

    private static class IndexCounter {

        int base = 0;

        int next() {
            return base++;
        }

    }



}
