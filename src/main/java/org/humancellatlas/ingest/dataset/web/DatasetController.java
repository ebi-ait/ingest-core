package org.humancellatlas.ingest.dataset.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.dataset.Dataset;
import org.humancellatlas.ingest.dataset.DatasetService;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.security.CheckAllowed;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.exception.NotAllowedDuringSubmissionStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestController
@ExposesResourceFor(Dataset.class)
@RequiredArgsConstructor
@Getter
public class DatasetController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);
    private final @NonNull DatasetService datasetService;

    @PostMapping("/datasets")
    public ResponseEntity<Resource<?>> register(@RequestBody final Dataset dataset, final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.register(dataset)));
    }

    @PatchMapping("/datasets/{datasetId}")
    public ResponseEntity<Resource<?>> update(@PathVariable final String datasetId, @RequestBody final ObjectNode patch, final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.update(datasetId, patch)));
    }

    @DeleteMapping("/datasets/{datasetId}")
    public ResponseEntity<Void> delete(@PathVariable final String datasetId) {
        datasetService.delete(datasetId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/datasets/{datasetId}")
    public ResponseEntity<Resource<?>> replace(@PathVariable final String datasetId, @RequestBody final Dataset updatedDataset, final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.replace(datasetId, updatedDataset)));
    }

    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PostMapping(path = "submissionEnvelopes/{sub_id}/datasets")
    ResponseEntity<Resource<?>> addDatasetToEnvelope(@PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope, @RequestBody final Dataset dataset, @RequestParam("updatingUuid") final Optional<UUID> updatingUuid, final PersistentEntityResourceAssembler assembler) {
        updatingUuid.ifPresent(uuid -> {
            dataset.setUuid(new Uuid(uuid.toString()));
            dataset.setIsUpdate(true);
        });

        final Dataset entity = datasetService.addDatasetToSubmissionEnvelope(submissionEnvelope, dataset);
        final PersistentEntityResource resource = assembler.toFullResource(entity);

        return ResponseEntity.accepted().body(resource);
    }

    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PutMapping(path = "datasets/{dataset_id}/submissionEnvelopes/{sub_id}")
    ResponseEntity<Resource<?>> linkSubmissionToDataset(@PathVariable("dataset_id") final Dataset dataset, @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope, final PersistentEntityResourceAssembler assembler) {
        final Dataset savedDataset = datasetService.linkDatasetSubmissionEnvelope(submissionEnvelope, dataset);
        final PersistentEntityResource datasetResource = assembler.toFullResource(savedDataset);

        return ResponseEntity.accepted().body(datasetResource);
    }

    @PostMapping("datasets/{dataset_id}/biomaterial/{biomaterial_id}")
    public ResponseEntity<Resource<?>> linkBiomaterialToDataset(@PathVariable final Dataset dataset,
                                                                @PathVariable final Biomaterial biomaterial,
                                                                final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.accepted().body(
                assembler.toFullResource(getDatasetService().linkBiomaterialToDataset(dataset, biomaterial)));
    }

    @PutMapping("datasets/{dataset_id}/file/{file_id}")
    public ResponseEntity<Resource<?>> linkFileToDataset(@PathVariable("dataset_id") final Dataset dataset,
                                                         @PathVariable("file_id") final File file,
                                                         final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.accepted().body(
                assembler.toFullResource(getDatasetService().linkFileToDataset(dataset, file)));
    }

    @PostMapping("/{dataset_id}/protocol/{protocol_id}")
    public ResponseEntity<Resource<?>> linkProtocolToDataset(@PathVariable final Dataset dataset,
                                                             @PathVariable final Protocol protocol,
                                                             final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.accepted().body(
                assembler.toFullResource(getDatasetService().linkProtocolToDataset(dataset, protocol)));
    }

    @PostMapping("/{dataset_id}/process/{process_id}")
    public ResponseEntity<Resource<?>> linkProcessToDataset(@PathVariable final Dataset dataset,
                                                            @PathVariable final Process process,
                                                            final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.accepted().body(
                assembler.toFullResource(getDatasetService().linkProcessToDataset(dataset, process)));
    }
}
