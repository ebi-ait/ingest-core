package uk.ac.ebi.subs.ingest.study.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
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
import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.subs.ingest.core.Uuid;
import uk.ac.ebi.subs.ingest.dataset.Dataset;
import uk.ac.ebi.subs.ingest.gene.GeneSearchClient;
import uk.ac.ebi.subs.ingest.security.CheckAllowed;
import uk.ac.ebi.subs.ingest.study.Study;
import uk.ac.ebi.subs.ingest.study.StudyRepository;
import uk.ac.ebi.subs.ingest.study.StudyService;
import uk.ac.ebi.subs.ingest.study.api.GeneRef;
import uk.ac.ebi.subs.ingest.study.api.StudyDto;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;
import uk.ac.ebi.subs.ingest.submission.exception.NotAllowedDuringSubmissionStateException;

@RepositoryRestController
@ExposesResourceFor(Study.class)
@RequiredArgsConstructor
@Getter
@Slf4j
/*
Controller for studies
 */
public class StudyController {
  private static final Logger LOGGER = LoggerFactory.getLogger(StudyController.class);
  private final @NonNull StudyService studyService;
  private final @NonNull StudyRepository studyRepository;
  private final GeneSearchClient geneClient;

  @Value("${gene.api.base}")
  private String geneApiBase;

  @PatchMapping("/studies/{studyId}")
  public ResponseEntity<Resource<?>> updateStudy(
      @PathVariable("studyId") final String studyId, // Change to String
      @RequestBody final ObjectNode patch,
      final PersistentEntityResourceAssembler assembler) {

    // Fetch the Study object in the service layer based on studyId
    Study study = studyService.findById(studyId); // New service method to get Study
    return ResponseEntity.ok().body(assembler.toFullResource(studyService.update(study, patch)));
  }

  @DeleteMapping("/studies/{studyId}")
  public ResponseEntity<Void> deleteStudy(@PathVariable final String studyId) {
    studyService.delete(studyId);

    return ResponseEntity.noContent().build();
  }

  // TODO: merge add and link, no reason to have both
  // @PreAuthorize("hasAnyRole('ROLE_CONTRIBUTOR', 'ROLE_WRANGLER', 'ROLE_SERVICE')")
  @CheckAllowed(
      value = "#submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PostMapping(path = "submissionEnvelopes/{sub_id}/studies")
  public ResponseEntity<Resource<?>> addStudyToEnvelopeAndLink(
      @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
      @RequestBody final Study study,
      @RequestParam("updatingUuid") final Optional<UUID> updatingUuid,
      final PersistentEntityResourceAssembler assembler) {
    updatingUuid.ifPresent(
        uuid -> {
          study.setUuid(new Uuid(uuid.toString()));
          study.setIsUpdate(true);
        });

    final Study savedStudy =
        getStudyService().addStudyToSubmissionEnvelope(submissionEnvelope, study);
    final PersistentEntityResource resource =
        assembler.toFullResource(
            getStudyService().linkStudyToSubmissionEnvelope(submissionEnvelope, savedStudy));

    return ResponseEntity.accepted().body(resource);
  }

  @CheckAllowed(
      value = "#submissionEnvelope.isSystemEditable()",
      exception = NotAllowedDuringSubmissionStateException.class)
  @PutMapping(path = "submissionEnvelopes/{sub_id}/studies/{stud_id}/")
  public ResponseEntity<Resource<?>> linkSubmissionToStudy(
      @PathVariable("sub_id") final SubmissionEnvelope submissionEnvelope,
      @PathVariable("stud_id") final Study study,
      final PersistentEntityResourceAssembler assembler) {
    final Study savedStudy =
        getStudyService().linkStudyToSubmissionEnvelope(submissionEnvelope, study);
    final PersistentEntityResource studyResource = assembler.toFullResource(savedStudy);

    return ResponseEntity.accepted().body(studyResource);
  }

  @PutMapping(path = "studies/{stud_id}/datasets/{dataset_id}")
  public ResponseEntity<Resource<?>> linkDatasetToStudy(
      @PathVariable("stud_id") final Study study,
      @PathVariable("dataset_id") final Dataset dataset,
      final PersistentEntityResourceAssembler assembler) {
    return ResponseEntity.accepted()
        .body(assembler.toFullResource(getStudyService().linkDatasetToStudy(study, dataset)));
  }

  @GetMapping("/studies/{studyId}")
  public ResponseEntity<StudyDto> getStudy(@PathVariable String studyId) {
    return Optional.ofNullable(studyService.findById(studyId))
        .map(
            study -> {

              // ---------------- build the enriched list ----------------
              log.debug("Loaded Study {}, targetGenes = {}", studyId, study.getTargetGenes());
              List<GeneRef> enriched =
                  study.getTargetGenes().stream()
                      .map(
                          symbol ->
                              geneClient
                                  .symbolToHgncId(symbol) // Optional<String>
                                  .map(id -> new GeneRef(symbol, geneClient.buildGeneUrl(id)))
                                  .orElse(new GeneRef(symbol, null)))
                      .collect(Collectors.toList());
              // --------------------------------------------------------
              log.debug("After enrichment Study {}", new StudyDto(study, enriched));
              return ResponseEntity.ok(new StudyDto(study, enriched));
            })
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
