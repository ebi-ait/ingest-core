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

/**
 * Controller for managing Datasets.
 */
@RepositoryRestController
@ExposesResourceFor(Dataset.class)
@RequiredArgsConstructor
@Getter
public class DatasetController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);
    private final @NonNull DatasetService datasetService;

    /**
     * Register a new dataset.
     *
     * @param dataset   The dataset to register.
     * @param assembler The resource assembler.
     * @return The registered dataset as a resource.
     */
    @PostMapping("/datasets")
    public ResponseEntity<Resource<?>> register(@RequestBody final Dataset dataset, final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.register(dataset)));
    }

    /**
     * Update an existing dataset.
     *
     * @param dataset   The dataset to update.
     * @param patch     The patch containing updates.
     * @param assembler The resource assembler.
     * @return The updated dataset as a resource.
     */
    @PatchMapping("/datasets/{datasetId}")
    public ResponseEntity<Resource<?>> update(@PathVariable("datasetId") final Dataset dataset,
                                              @RequestBody final ObjectNode patch,
                                              final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.update(dataset, patch)));
    }

    /**
     * Delete a dataset.
     *
     * @param datasetId The ID of the dataset to delete.
     * @return No content response.
     */
    @DeleteMapping("/datasets/{datasetId}")
    public ResponseEntity<Void> delete(@PathVariable final String datasetId) {
        datasetService.delete(datasetId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Replace an existing dataset.
     *
     * @param datasetId      The ID of the dataset to replace.
     * @param updatedDataset The new dataset.
     * @param assembler      The resource assembler.
     * @return The replaced dataset as a resource.
     */
    @PutMapping("/datasets/{datasetId}")
    public ResponseEntity<Resource<?>> replace(@PathVariable final String datasetId, @RequestBody final Dataset updatedDataset, final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.replace(datasetId, updatedDataset)));
    }

    /**
     * Add a dataset to a submission envelope.
     *
     * @param submissionEnvelope The submission envelope.
     * @param dataset            The dataset to add.
     * @param updatingUuid       Optional UUID for updating.
     * @param assembler          The resource assembler.
     * @return The added dataset as a resource.
     */
    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PostMapping(path = "submissionEnvelopes/{sub_id}/datasets")
    public ResponseEntity<Resource<?>> addDatasetToEnvelope(@PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope, @RequestBody final Dataset dataset, @RequestParam("updatingUuid") final Optional<UUID> updatingUuid, final PersistentEntityResourceAssembler assembler) {
        updatingUuid.ifPresent(uuid -> {
            dataset.setUuid(new Uuid(uuid.toString()));
            dataset.setIsUpdate(true);
        });

        final Dataset entity = datasetService.addDatasetToSubmissionEnvelope(submissionEnvelope, dataset);
        final PersistentEntityResource resource = assembler.toFullResource(entity);

        return ResponseEntity.accepted().body(resource);
    }

    /**
     * Link a submission envelope to a dataset.
     *
     * @param dataset            The dataset to link.
     * @param submissionEnvelope The submission envelope.
     * @param assembler          The resource assembler.
     * @return The linked dataset as a resource.
     */
    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PutMapping(path = "submissionEnvelopes/{sub_id}/datasets/{dataset_id}")
    public ResponseEntity<Resource<?>> linkSubmissionToDataset(@PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
                                                               @PathVariable("dataset_id") final Dataset dataset,
                                                               final PersistentEntityResourceAssembler assembler) {
        final Dataset savedDataset = datasetService.linkDatasetSubmissionEnvelope(submissionEnvelope, dataset);
        final PersistentEntityResource datasetResource = assembler.toFullResource(savedDataset);

        return ResponseEntity.accepted().body(datasetResource);
    }

    /**
     * Link a biomaterial to a dataset.
     *
     * @param dataset     The dataset.
     * @param biomaterial The biomaterial to link.
     * @param assembler   The resource assembler.
     * @return The updated dataset as a resource.
     */
    @PutMapping("datasets/{dataset_id}/biomaterial/{biomaterial_id}")
    public ResponseEntity<Resource<?>> linkBiomaterialToDataset(@PathVariable("dataset_id") final Dataset dataset, @PathVariable("biomaterial_id") final Biomaterial biomaterial, final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.accepted().body(assembler.toFullResource(datasetService.linkBiomaterialToDataset(dataset, biomaterial)));
    }

    /**
     * Link a file to a dataset.
     *
     * @param dataset   The dataset.
     * @param file      The file to link.
     * @param assembler The resource assembler.
     * @return The updated dataset as a resource.
     */
    @PutMapping("datasets/{dataset_id}/file/{file_id}")
    public ResponseEntity<Resource<?>> linkFileToDataset(@PathVariable("dataset_id") final Dataset dataset, @PathVariable("file_id") final File file, final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.accepted().body(assembler.toFullResource(datasetService.linkFileToDataset(dataset, file)));
    }

    /**
     * Link a protocol to a dataset.
     *
     * @param dataset   The dataset.
     * @param protocol  The protocol to link.
     * @param assembler The resource assembler.
     * @return The updated dataset as a resource.
     */
    @PutMapping("/{dataset_id}/protocol/{protocol_id}")
    public ResponseEntity<Resource<?>> linkProtocolToDataset(@PathVariable("dataset_id") final Dataset dataset, @PathVariable("protocol_id") final Protocol protocol, final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.accepted().body(assembler.toFullResource(datasetService.linkProtocolToDataset(dataset, protocol)));
    }

    /**
     * Link a process to a dataset.
     *
     * @param dataset   The dataset.
     * @param process   The process to link.
     * @param assembler The resource assembler.
     * @return The updated dataset as a resource.
     */
    @PutMapping("/{dataset_id}/process/{process_id}")
    public ResponseEntity<Resource<?>> linkProcessToDataset(@PathVariable("dataset_id") final Dataset dataset, @PathVariable("process_id") final Process process, final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.accepted().body(assembler.toFullResource(datasetService.linkProcessToDataset(dataset, process)));
    }
}
