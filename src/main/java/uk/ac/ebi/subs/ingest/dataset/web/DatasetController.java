package uk.ac.ebi.subs.ingest.dataset.web;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.dataset.Dataset;
import uk.ac.ebi.subs.ingest.dataset.DatasetService;
import uk.ac.ebi.subs.ingest.security.CheckAllowed;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.exception.NotAllowedDuringSubmissionStateException;

/** Controller for managing Datasets. */
@RepositoryRestController
@ExposesResourceFor(Dataset.class)
@RequiredArgsConstructor
@Getter
public class DatasetController {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);
  private final @NonNull DatasetService datasetService;

  /**
   * Update an existing dataset.
   *
   * @param dataset The dataset to update.
   * @param patch The patch containing updates.
   * @param assembler The resource assembler.
   * @return The updated dataset as a resource.
   */
  @PatchMapping("/datasets/{datasetId}")
  public ResponseEntity<Resource<?>> update(
      @PathVariable("datasetId") final Dataset dataset,
      @RequestBody final ObjectNode patch,
      final PersistentEntityResourceAssembler assembler) {
    return ResponseEntity.ok()
        .body(assembler.toFullResource(datasetService.update(dataset, patch)));
  }

  /**
   * Delete a dataset.
   *
   * @param datasetId The ID of the dataset to delete.
   * @return No content response.
   */
  // TODO: check why not authenticated
  @DeleteMapping("/datasets/{datasetId}")
  public ResponseEntity<Void> delete(
      @PathVariable final String datasetId,
      @RequestParam(name = "deleteLinkedEntities", required = false, defaultValue = "false")
          boolean deleteLinkedEntities) {
    datasetService.delete(datasetId, deleteLinkedEntities);
    return ResponseEntity.noContent().build();
  }

  /**
   * Add a dataset to a submission envelope.
   *
   * @param submissionEnvelope The submission envelope.
   * @param dataset The dataset to add.
   * @param updatingUuid Optional UUID for updating.
   * @param assembler The resource assembler.
   * @return The added dataset as a resource.
   */
  @CheckAllowed(
      value = "#submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PostMapping(path = "/submissionEnvelopes/{sub_id}/datasets")
  public ResponseEntity<Resource<?>> addDatasetToEnvelopeAndLink(
      @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
      @RequestBody final Dataset dataset,
      @RequestParam("updatingUuid") final Optional<UUID> updatingUuid,
      final PersistentEntityResourceAssembler assembler) {
    updatingUuid.ifPresent(
        uuid -> {
          dataset.setUuid(new Uuid(uuid.toString()));
          dataset.setIsUpdate(true);
        });

    final Dataset savedDataset =
        datasetService.addDatasetToSubmissionEnvelope(submissionEnvelope, dataset);

    return ResponseEntity.accepted().body(assembler.toFullResource(savedDataset));
  }

  /**
   * Link a submission envelope to a dataset.
   *
   * @param dataset The dataset to link.
   * @param submissionEnvelope The submission envelope.
   * @param assembler The resource assembler.
   * @return The linked dataset as a resource.
   */
  @CheckAllowed(
      value = "#submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PutMapping(path = "/submissionEnvelopes/{sub_id}/datasets/{dataset_id}")
  public ResponseEntity<Resource<?>> linkSubmissionToDataset(
      @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
      @PathVariable("dataset_id") final Dataset dataset,
      final PersistentEntityResourceAssembler assembler) {
    final Dataset savedDataset =
        datasetService.addDatasetToSubmissionEnvelope(submissionEnvelope, dataset);

    return ResponseEntity.accepted().body(assembler.toFullResource(savedDataset));
  }

  /**
   * Link a biomaterial to a dataset.
   *
   * @param dataset The dataset.
   * @param id The id of the biomaterial to link.
   * @param assembler The resource assembler.
   * @return The updated dataset as a resource.
   */
  @PutMapping("/datasets/{dataset_id}/biomaterials/{biomaterial_id}")
  public ResponseEntity<Resource<?>> addBiomaterialToDataset(
      @PathVariable("dataset_id") final Dataset dataset,
      @PathVariable("biomaterial_id") final String id,
      final PersistentEntityResourceAssembler assembler) {
    return ResponseEntity.accepted()
        .body(assembler.toFullResource(datasetService.addBiomaterialToDataset(dataset, id)));
  }

  /**
   * Link a file to a dataset.
   *
   * @param dataset The dataset.
   * @param id The id of the file to link.
   * @param assembler The resource assembler.
   * @return The updated dataset as a resource.
   */
  @PutMapping("/datasets/{dataset_id}/files/{file_id}")
  public ResponseEntity<Resource<?>> addFileToDataset(
      @PathVariable("dataset_id") final Dataset dataset,
      @PathVariable("file_id") final String id,
      final PersistentEntityResourceAssembler assembler) {
    return ResponseEntity.accepted()
        .body(assembler.toFullResource(datasetService.addFileToDataset(dataset, id)));
  }

  /**
   * Link a process to a dataset.
   *
   * @param dataset The dataset.
   * @param id The id of the process to link.
   * @param assembler The resource assembler.
   * @return The updated dataset as a resource.
   */
  @PutMapping("/datasets/{dataset_id}/processes/{process_id}")
  public ResponseEntity<Resource<?>> addProcessToDataset(
      @PathVariable("dataset_id") final Dataset dataset,
      @PathVariable("process_id") final String id,
      final PersistentEntityResourceAssembler assembler) {
    return ResponseEntity.accepted()
        .body(assembler.toFullResource(datasetService.addProcessToDataset(dataset, id)));
  }
}
