package org.humancellatlas.ingest.export;

import org.apache.commons.collections4.ListUtils;
import org.humancellatlas.ingest.bundle.BundleManifest;
import org.humancellatlas.ingest.bundle.BundleManifestRepository;
import org.humancellatlas.ingest.bundle.BundleManifestService;
import org.humancellatlas.ingest.core.EntityType;
import org.humancellatlas.ingest.core.MetadataDocument;
import org.humancellatlas.ingest.core.MetadataDocumentMessageBuilder;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.core.service.MetadataCrudService;
import org.humancellatlas.ingest.core.web.LinkGenerator;
import org.humancellatlas.ingest.messaging.MessageRouter;
import org.humancellatlas.ingest.messaging.model.BundleUpdateMessage;
import org.humancellatlas.ingest.process.ProcessService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class DefaultExporter implements Exporter {

    @Autowired
    private ProcessService processService;

    @Autowired
    private MetadataCrudService metadataCrudService;

    @Autowired
    private BundleManifestService bundleManifestService;

    @Autowired
    private BundleManifestRepository bundleManifestRepository;

    @Autowired
    private MessageRouter messageRouter;

    @Autowired
    private LinkGenerator linkGenerator;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void exportBundles(SubmissionEnvelope envelope) {
        Collection<String> assayingProcessIds = processService.findAssays(envelope);
        Collection<String> analysisProcessIds = processService.findAnalyses(envelope);

        log.info(String.format("Found %s assays and %s analysis processes for envelope with ID %s",
                               assayingProcessIds.size(),
                               analysisProcessIds.size(),
                               envelope.getId()));

        IndexCounter counter = new IndexCounter();
        int totalCount = assayingProcessIds.size() + analysisProcessIds.size();

        int partitionSize = 500;
        partitionProcessIds(assayingProcessIds, partitionSize)
                .stream()
                .map(processIdBatch -> processService.getProcesses(processIdBatch))
                .flatMap(Function.identity())
                .map(process -> new ExportData(counter.next(), totalCount, process,  envelope))
                .forEach(messageRouter::sendAssayForExport);

        partitionProcessIds(analysisProcessIds, partitionSize)
                .stream()
                .map(processIdBatch -> processService.getProcesses(processIdBatch))
                .flatMap(Function.identity())
                .map(process -> new ExportData(counter.next(), totalCount, process,  envelope))
                .forEach(messageRouter::sendAnalysisForExport);
    }

    @Override
    public void updateBundles(SubmissionEnvelope submissionEnvelope) {
    	// FIXME why do we need a whole collection of metadocument here?
    	
        Stream projects = metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROJECT);
        Stream biomaterials = metadataCrudService.findBySubmission(submissionEnvelope, EntityType.BIOMATERIAL);
        Stream protocols = metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROTOCOL);
        Stream processes = metadataCrudService.findBySubmission(submissionEnvelope, EntityType.PROCESS);
        Stream files = metadataCrudService.findBySubmission(submissionEnvelope, EntityType.FILE);
        
		Stream<MetadataDocument> documentsToUpdate = Stream.of(projects, biomaterials, protocols, processes, files).flatMap(i->i);
        
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
            Optional<BundleManifest> maybeBundleManifest = bundleManifestRepository.findTopByBundleUuidOrderByBundleVersionDesc(bundleManifestUuid);
            BundleUpdateMessage exportMessage = maybeBundleManifest.map(bundleManifest -> builder.buildBundleUpdateMessage(bundleManifest, bundleManifestsToUpdate.get(bundleManifestUuid)))
                                                                   .orElseThrow(() -> {
                                                                       throw new RuntimeException(String.format("Failed to find a bundle manifest for bundle UUID %s", bundleManifestUuid));
                                                                   });

            return exportMessage;
        }).forEach(messageRouter::sendBundlesToUpdateForExport);
    }

    /**
     *
     * Divides a set of process IDs into lists of size partitionSize
     *
     * @param processIds
     * @param partitionSize
     * @return A collection of partitionSize sized lists of processes
     */
    private static List<List<String>> partitionProcessIds(Collection<String> processIds, int partitionSize) {
        return ListUtils.partition(new ArrayList<>(processIds), partitionSize);
    }


    private static class IndexCounter {

        int base = 0;

        int next() {
            return base++;
        }

    }

}
