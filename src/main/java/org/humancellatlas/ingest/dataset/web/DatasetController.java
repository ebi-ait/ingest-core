package org.humancellatlas.ingest.dataset.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.dataset.Dataset;
import org.humancellatlas.ingest.dataset.DatasetService;
import org.humancellatlas.ingest.security.CheckAllowed;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submission.exception.NotAllowedDuringSubmissionStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
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
    private final Environment environment;

    @PostMapping("/datasets")
    public ResponseEntity<Resource<?>> register(@RequestBody final Dataset dataset,
                                                final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.register(dataset)));
    }

    @PatchMapping("/datasets/{datasetId}")
    public ResponseEntity<Resource<?>> update(@PathVariable final String datasetId,
                                              @RequestBody final ObjectNode patch,
                                              final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.update(datasetId, patch)));
    }

    @DeleteMapping("/datasets/{datasetId}")
    public ResponseEntity<Void> delete(@PathVariable final String datasetId) {
        datasetService.delete(datasetId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/datasets/{datasetId}")
    public ResponseEntity<Resource<?>> replace(@PathVariable final String datasetId,
                                               @RequestBody final Dataset updatedDataset,
                                               final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.replace(datasetId, updatedDataset)));
    }

    @CheckAllowed(value = "#submissionEnvelope.isSystemEditable()", exception = NotAllowedDuringSubmissionStateException.class)
    @PostMapping(path = "submissionEnvelopes/{sub_id}/datasets")
    ResponseEntity<Resource<?>> addDatasetToEnvelope(
            @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
            @RequestBody final Dataset dataset,
            @RequestParam("updatingUuid") final Optional<UUID> updatingUuid,
            final PersistentEntityResourceAssembler assembler) {
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
    ResponseEntity<Resource<?>> linkSubmissionToDataset(
            @PathVariable("dataset_id") final Dataset dataset,
            @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
            final PersistentEntityResourceAssembler assembler) {
        final Dataset savedDataset = datasetService.linkDatasetSubmissionEnvelope(submissionEnvelope, dataset);
        final PersistentEntityResource datasetResource = assembler.toFullResource(savedDataset);

        return ResponseEntity.accepted().body(datasetResource);
    }
}
