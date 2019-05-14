package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestService;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.messaging.model.ExportMessage;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class UpdateExporter implements Exporter {

    @Autowired
    private BundleManifestService bundleManifestService;

    @Autowired
    private MetadataCrudService metadataCrudService;

    @Autowired
    private MessageRouter messageRouter;

    @Autowired
    private LinkGenerator linkGenerator;

    @Override
    public void exportBundles(SubmissionEnvelope submissionEnvelope) {
        Collection<MetadataDocument> documentsToUpdate = new ArrayList<>();
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROJECT));
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.BIOMATERIAL));
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROTOCOL));
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROCESS));
        documentsToUpdate.addAll(metadataCrudService.findBySubmission(submissionEnvelope, EntityType.FILE));

        Collection<BundleManifest> bundleManifestsToUpdate = bundleManifestService.bundleManifestsForDocuments(documentsToUpdate);
        int totalCount = bundleManifestsToUpdate.size();

        IndexCounter counter = new IndexCounter();
        Uuid submissionUuid = submissionEnvelope.getUuid();
        bundleManifestsToUpdate.stream().map(bundleManifest -> {
            MetadataDocumentMessageBuilder builder = MetadataDocumentMessageBuilder.using(linkGenerator)
                    .withEnvelopeId(submissionEnvelope.getId())
                    .withAssayIndex(counter.next())
                    .withTotalAssays(totalCount);
            if(submissionUuid != null && submissionUuid.getUuid() != null){
                builder.withEnvelopeUuid(submissionUuid.getUuid().toString());
            }
            ExportMessage exportMessage = builder.buildUpdateExportMessage(bundleManifest);
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
