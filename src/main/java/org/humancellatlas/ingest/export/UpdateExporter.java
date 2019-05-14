package org.humancellatlas.ingest.export;

import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestService;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.file.FileRepository;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.project.ProjectRepository;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collection;

public class UpdateExporter implements Exporter {

    @Autowired
    private BundleManifestService bundleManifestService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BiomaterialRepository biomaterialRepository;

    @Autowired
    private ProtocolRepository protocolRepository;

    @Autowired
    private ProcessRepository processRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private MessageRouter messageRouter;

    private LinkGenerator linkGenerator;

    private MetadataDocumentMessageBuilder metadataDocumentMessageBuilder;

    @Override
    public void exportBundles(SubmissionEnvelope submissionEnvelope) {
        Collection<MetadataDocument> documentsToUpdate = new ArrayList<>();
        Pageable unpaged = new PageRequest(0, Integer.MAX_VALUE);
        documentsToUpdate.addAll(projectRepository.findBySubmissionEnvelopesContaining(submissionEnvelope, unpaged).getContent());
        documentsToUpdate.addAll(biomaterialRepository.findBySubmissionEnvelopesContaining(submissionEnvelope, unpaged).getContent());
        documentsToUpdate.addAll(protocolRepository.findBySubmissionEnvelopesContaining(submissionEnvelope, unpaged).getContent());
        documentsToUpdate.addAll(processRepository.findBySubmissionEnvelopesContaining(submissionEnvelope));
        documentsToUpdate.addAll(fileRepository.findBySubmissionEnvelopesContaining(submissionEnvelope));

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
            return metadataDocumentMessageBuilder.buildUpdateExportMessage(bundleManifest);
        }).forEach(messageRouter::sendBundlesToUpdateForExport);
    }



    private static class IndexCounter {

        int base = 0;

        int next() {
            return base++;
        }

    }
}
