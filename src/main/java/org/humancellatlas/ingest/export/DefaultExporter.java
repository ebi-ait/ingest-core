package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.bundle.BundleManifestService;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.messaging.model.BundleUpdateMessage;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Component
public class DefaultExporter implements Exporter {

    @Autowired
    private ProcessService processService;

    @Autowired
    private MetadataCrudService metadataCrudService;

    @Autowired
    private BundleManifestService bundleManifestService;

    @Autowired
    private MessageRouter messageRouter;

    @Autowired
    private LinkGenerator linkGenerator;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void exportBundles(SubmissionEnvelope envelope) {
        Collection<Process> assayingProcesses = processService.findAssays(envelope);
        Collection<Process> analysisProcesses = processService.findAnalyses(envelope);

        log.info(String.format("Found %s assays and %s analysis processes for envelope with ID %s",
                               assayingProcesses.size(),
                               analysisProcesses.size(),
                               envelope.getId()));

        IndexCounter counter = new IndexCounter();
        int totalCount = assayingProcesses.size() + analysisProcesses.size();
        assayingProcesses.stream()
                .map(process -> new ExportData(counter.next(), totalCount, process,  envelope))
                .forEach(messageRouter::sendAssayForExport);
        analysisProcesses.stream()
                .map(process -> new ExportData(counter.next(), totalCount, process, envelope))
                .forEach(messageRouter::sendAnalysisForExport);
    }

    @Override
    public void updateBundles(SubmissionEnvelope submissionEnvelope) {
        Collection<MetadataDocument> documentsToUpdate = new ArrayList<>();
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROJECT));
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.BIOMATERIAL));
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROTOCOL));
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROCESS));
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.FILE));

        Map<String, Set<MetadataDocument>> bundleManifestsToUpdate = bundleManifestService.bundleManifestsForDocuments(documentsToUpdate);
        int totalCount = bundleManifestsToUpdate.size();

        IndexCounter counter = new IndexCounter();
        Uuid submissionUuid = submissionEnvelope.getUuid();
        bundleManifestsToUpdate.keySet().stream().map(bundleManifestUuid -> {
            MetadataDocumentMessageBuilder builder = MetadataDocumentMessageBuilder.using(linkGenerator)
                    .withEnvelopeId(submissionEnvelope.getId())
                    .withAssayIndex(counter.next())
                    .withTotalAssays(totalCount);
            if(submissionUuid != null && submissionUuid.getUuid() != null){
                builder.withEnvelopeUuid(submissionUuid.getUuid().toString());
            }
            BundleUpdateMessage exportMessage = builder.buildBundleUpdateMessage(bundleManifestUuid, bundleManifestsToUpdate.get(bundleManifestUuid));
            return exportMessage;
        }).forEach(messageRouter::sendBundlesToUpdateForExport);
    }


    private static class IndexCounter {

        int base = 0;

        int next() {
            return base++;
        }

    }

}
