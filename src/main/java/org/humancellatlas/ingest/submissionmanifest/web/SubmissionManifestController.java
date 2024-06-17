package org.humancellatlas.ingest.submissionmanifest.web;

import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifest;
import org.humancellatlas.ingest.submissionmanifest.SubmissionManifestRepository;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/** Created by rolando on 30/05/2018. */
@RepositoryRestController
@ExposesResourceFor(SubmissionManifest.class)
@RequiredArgsConstructor
@Getter
public class SubmissionManifestController {
  private final @NonNull SubmissionManifestRepository submissionManifestRepository;

  @RequestMapping(
      path = "submissionEnvelopes/{sub_id}/submissionManifest",
      method = RequestMethod.POST)
  ResponseEntity<Resource<?>> addManifestToEnvelope(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @RequestBody SubmissionManifest submissionManifest,
      PersistentEntityResourceAssembler assembler) {
    submissionManifest.setSubmissionEnvelope(submissionEnvelope);
    SubmissionManifest manifest = submissionManifestRepository.save(submissionManifest);
    PersistentEntityResource resource = assembler.toFullResource(manifest);
    return ResponseEntity.accepted().body(resource);
  }
}
