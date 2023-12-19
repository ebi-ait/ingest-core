package org.humancellatlas.ingest.dataset.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.dataset.Dataset;
import org.humancellatlas.ingest.dataset.DatasetRepository;
import org.humancellatlas.ingest.dataset.DatasetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Javadocs!
 */
@RepositoryRestController
@ExposesResourceFor(Dataset.class)
@RequiredArgsConstructor
@Getter
public class DatasetController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);
    private final @NonNull DatasetService datasetService;

    @PostMapping("/datasets")
    public ResponseEntity<Resource<?>> register(@RequestBody final Dataset dataset,
                                                final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.register(dataset)));
    }

    @PatchMapping("/datasets/{datasetId}")
    public ResponseEntity<Resource<?>> update(@PathVariable String datasetId,
                                              @RequestBody final ObjectNode patch,
                                              final PersistentEntityResourceAssembler assembler) {
        return ResponseEntity.ok().body(assembler.toFullResource(datasetService.update(datasetId, patch)));
    }

    @DeleteMapping("/datasets/{datasetId}")
    public ResponseEntity<Void> delete(@PathVariable String datasetId) {
        datasetService.delete(datasetId);

        return ResponseEntity.noContent().build();
    }

}
