package uk.ac.ebi.subs.ingest.dataset.web;

import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.dataset.Dataset;
import uk.ac.ebi.subs.ingest.dataset.DatasetService;
import uk.ac.ebi.subs.ingest.dataset.FileListingEntry;
import uk.ac.ebi.subs.ingest.dataset.util.GlobusService;
import uk.ac.ebi.subs.ingest.dataset.util.UploadAreaUtilGlobus;
import uk.ac.ebi.subs.ingest.security.CheckAllowed;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.exception.NotAllowedDuringSubmissionStateException;

/** Controller for managing Datasets. */
@RepositoryRestController
@ExposesResourceFor(Dataset.class)
@RequiredArgsConstructor
@Getter
@Slf4j
public class DatasetController {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatasetController.class);
  private final @NonNull DatasetService datasetService;
  private final GlobusService globus;
  private final UploadAreaUtilGlobus uploadAreaUtilGlobus;

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
      @RequestHeader(value = "X-Globus-Identity", required = true) final String globusIdentityId,
      final PersistentEntityResourceAssembler assembler) {

    updatingUuid.ifPresent(
        uuid -> {
          dataset.setUuid(new Uuid(uuid.toString()));
          dataset.setIsUpdate(true);
        });

    dataset.setGlobusOwnerIdentityId(globusIdentityId);

    final Dataset savedDataset =
        datasetService.addDatasetToSubmissionEnvelope(
            submissionEnvelope, dataset, globusIdentityId);

    return ResponseEntity.accepted().body(assembler.toFullResource(savedDataset));
  }

  //  /**
  //   * Link a submission envelope to a dataset.
  //   *
  //   * @param dataset The dataset to link.
  //   * @param submissionEnvelope The submission envelope.
  //   * @param assembler The resource assembler.
  //   * @return The linked dataset as a resource.
  //   */
  //  @CheckAllowed(
  //      value = "#submissionEnvelope.isSystemEditable()",
  //      exception = NotAllowedDuringSubmissionStateException.class)
  //  @PutMapping(path = "/submissionEnvelopes/{sub_id}/datasets/{dataset_id}")
  //  public ResponseEntity<Resource<?>> linkSubmissionToDataset(
  //      @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
  //      @PathVariable("dataset_id") final Dataset dataset,
  //      final PersistentEntityResourceAssembler assembler) {
  //    final Dataset savedDataset =
  //        datasetService.addDatasetToSubmissionEnvelope(submissionEnvelope, dataset);
  //
  //    return ResponseEntity.accepted().body(assembler.toFullResource(savedDataset));
  //  }

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
  @PutMapping(
      value = "/datasets/{dataset_id}/files/{file_id}",
      consumes = MediaType.APPLICATION_JSON_VALUE)
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

  @PutMapping("/datasets/{dataset_id}/derivedFrom/{source_dataset_id}")
  public ResponseEntity<Resource<?>> addDerivedFromDataset(
      @PathVariable("dataset_id") final Dataset dataset,
      @PathVariable("source_dataset_id") final String sourceDatasetId,
      final PersistentEntityResourceAssembler assembler) {

    Dataset updatedDataset = datasetService.addDerivedFromDataset(dataset, sourceDatasetId);
    return ResponseEntity.accepted().body(assembler.toFullResource(updatedDataset));
  }

  @GetMapping("/datasets/{datasetId}/globus/area-exists")
  public ResponseEntity<Map<String, Object>> globusAreaExists(
      @PathVariable String datasetId, @RequestHeader("X-Globus-Identity") String callerGlobusId) {

    // Enforce ownership – throws 404/403 if not allowed
    Dataset dataset = assertDatasetOwner(datasetId, callerGlobusId);

    String root = globus.remoteDatasetRoot(datasetId);
    boolean exists = globus.directoryExists(root);

    return ResponseEntity.ok(
        Map.of(
            "datasetId", datasetId,
            "path", root,
            "exists", exists));
  }

  @GetMapping("/datasets/{datasetId}/globus/files")
  public ResponseEntity<List<FileListingEntry>> listGlobusFiles(
      @PathVariable String datasetId,
      @RequestParam(required = false, defaultValue = "") String prefix,
      @RequestHeader("X-Globus-Identity") String callerGlobusId) {

    // Enforce ownership
    Dataset dataset = assertDatasetOwner(datasetId, callerGlobusId);

    String root = globus.remoteDatasetRoot(datasetId);
    System.out.println("[Globus] listGlobusFiles root=" + root);

    if (!globus.directoryExists(root)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
    }

    try {
      List<GlobusService.GlobusEntry> entries = globus.listEntries(root);

      List<FileListingEntry> fileEntries =
          entries.stream()
              .filter(e -> prefix.isBlank() || e.getName().startsWith(prefix))
              .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
              .map(
                  e -> {
                    String name = e.getName();
                    String type = e.getType();

                    if ("dir".equalsIgnoreCase(type)) {
                      name += "/";
                    }

                    return new FileListingEntry(name, type, e.getSize());
                  })
              .collect(Collectors.toList());

      return ResponseEntity.ok(fileEntries);

    } catch (Exception e) {
      System.err.println("Globus list failed for dataset " + datasetId + ": " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/datasets/{datasetId}/globus/files/exists")
  public ResponseEntity<Map<String, Object>> fileExists(
      @PathVariable String datasetId,
      @RequestParam("path") String relPath,
      @RequestHeader("X-Globus-Identity") String callerGlobusId) {

    // Enforce ownership – throws 404/403 if not allowed
    Dataset dataset = assertDatasetOwner(datasetId, callerGlobusId);

    String root = globus.remoteDatasetRoot(datasetId);

    if (!globus.directoryExists(root)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              Map.of(
                  "datasetId",
                  datasetId,
                  "path",
                  root,
                  "exists",
                  false,
                  "reason",
                  "dataset area does not exist"));
    }

    try {
      boolean exists = globus.pathExists(root, relPath);

      if (!exists) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                Map.of(
                    "datasetId",
                    datasetId,
                    "path",
                    Paths.get(root, relPath).toString(),
                    "exists",
                    false));
      }

      return ResponseEntity.ok(
          Map.of(
              "datasetId", datasetId, "path", Paths.get(root, relPath).toString(), "exists", true));
    } catch (Exception e) {
      log.error(
          "Globus pathExists failed for dataset {} path {}: {}",
          datasetId,
          relPath,
          e.getMessage(),
          e);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/datasets/{datasetId}/delete")
  public ResponseEntity<Map<String, Object>> deleteGlobusFiles(
      @PathVariable String datasetId,
      @RequestBody DeleteRequest request,
      @RequestHeader("X-Globus-Identity") String callerGlobusId) {

    // Enforce ownership
    Dataset dataset = assertDatasetOwner(datasetId, callerGlobusId);

    String root = globus.remoteDatasetRoot(datasetId);

    if (!globus.directoryExists(root)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              Map.of(
                  "datasetId", datasetId,
                  "path", root,
                  "error", "dataset area does not exist"));
    }

    List<String> targets;
    try {
      if (request.isAllContents()) {
        List<GlobusService.GlobusEntry> entries = globus.listEntries(root);
        targets =
            entries.stream().map(GlobusService.GlobusEntry::getName).collect(Collectors.toList());
      } else {
        targets = request.getPaths();
      }
    } catch (Exception e) {
      log.error(
          "Globus list failed for dataset {} root {}: {}", datasetId, root, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to list dataset contents"));
    }

    if (targets == null || targets.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "No targets specified for deletion"));
    }

    try {
      Map<String, Object> deleteResp = globus.submitDelete(root, targets, request.isRecursive());

      Object taskIdObj = deleteResp.get("task_id");
      String taskId = taskIdObj != null ? taskIdObj.toString() : "unknown";

      Map<String, Object> body = new HashMap<>();
      body.put("delete_task_id", taskId);
      body.put("targets", targets);

      return ResponseEntity.accepted().body(body);
    } catch (Exception e) {
      log.error(
          "Globus delete failed for dataset {} root {}: {}", datasetId, root, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Globus delete failed: " + e.getMessage()));
    }
  }

  private Dataset assertDatasetOwner(String datasetId, String callerGlobusId) {
    Dataset dataset =
        datasetService
            .findById(datasetId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Dataset not found: " + datasetId));

    String ownerId = dataset.getGlobusOwnerIdentityId();

    //    if (ownerId == null || ownerId.isBlank()) {
    //      // If you prefer, you could make this 404 instead – but 403 is also reasonable.
    //      throw new ResponseStatusException(
    //              HttpStatus.FORBIDDEN,
    //              "Dataset has no Globus owner; access denied for caller " + callerGlobusId);
    //    }

    if ((ownerId == null || ownerId.isBlank()) || (!ownerId.equals(callerGlobusId))) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          String.format("Caller %s is not owner of dataset %s ", callerGlobusId, datasetId));
    }

    return dataset;
  }
}
