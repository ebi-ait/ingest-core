package org.humancellatlas.ingest.biomaterial.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialService;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.state.StateEngine;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by rolando on 16/02/2018.
 */
@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(Biomaterial.class)
@Getter
public class BiomaterialController {
  private final @NonNull BiomaterialService biomaterialService;
  private final @NonNull StateEngine stateEngine;

  @RequestMapping(path = "submissionEnvelopes/{sub_id}/biomaterials", method = RequestMethod.POST)
  ResponseEntity<Resource<?>> addBiomaterialToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @RequestBody Biomaterial biomaterial,
      PersistentEntityResourceAssembler assembler) {
    Biomaterial entity = getBiomaterialService().addBiomaterialToSubmissionEnvelope(submissionEnvelope, biomaterial);
    PersistentEntityResource resource = assembler.toFullResource(entity);
    return ResponseEntity.accepted().body(resource);
  }

  @RequestMapping(path = "submissionEnvelopes/{sub_id}/biomaterials/{id}", method = RequestMethod.PUT)
  ResponseEntity<Resource<?>> linkBiomaterialToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @PathVariable("id") Biomaterial biomaterial,
      PersistentEntityResourceAssembler assembler) {
    Biomaterial entity = getBiomaterialService().addBiomaterialToSubmissionEnvelope(submissionEnvelope, biomaterial);
    PersistentEntityResource resource = assembler.toFullResource(entity);
    return ResponseEntity.accepted().body(resource);
  }

  @RequestMapping(path = "/biomaterials/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
  HttpEntity<?> validatingBiomaterial(@PathVariable("id") Biomaterial biomaterial) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getBiomaterialService().getBiomaterialRepository(),
        biomaterial,
        ValidationState.VALIDATING);

    return ResponseEntity.accepted().body(event);
  }

  @RequestMapping(path = "/biomaterials/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
  HttpEntity<?> validateBiomaterial(@PathVariable("id") Biomaterial biomaterial) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getBiomaterialService().getBiomaterialRepository(),
        biomaterial,
        ValidationState.VALID);

    return ResponseEntity.accepted().body(event);
  }

  @RequestMapping(path = "/biomaterials/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
  HttpEntity<?> invalidateBiomaterial(@PathVariable("id") Biomaterial biomaterial) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getBiomaterialService().getBiomaterialRepository(),
        biomaterial,
        ValidationState.INVALID);

    return ResponseEntity.accepted().body(event);
  }

  @RequestMapping(path = "/biomaterials/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
  HttpEntity<?> processingBiomaterial(@PathVariable("id") Biomaterial biomaterial) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getBiomaterialService().getBiomaterialRepository(),
        biomaterial,
        ValidationState.PROCESSING);

    return ResponseEntity.accepted().body(event);
  }

  @RequestMapping(path = "/biomaterials/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
  HttpEntity<?> completeBiomaterial(@PathVariable("id") Biomaterial biomaterial) {
    Event event = this.getStateEngine().advanceStateOfMetadataDocument(
        getBiomaterialService().getBiomaterialRepository(),
        biomaterial,
        ValidationState.COMPLETE);

    return ResponseEntity.accepted().body(event);
  }
}
