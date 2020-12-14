package org.humancellatlas.ingest.biomaterial.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialRepository;
import org.humancellatlas.ingest.biomaterial.BiomaterialService;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.file.File;
import org.humancellatlas.ingest.patch.JsonPatcher;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by rolando on 16/02/2018.
 */
@RepositoryRestController
@RequiredArgsConstructor
@ExposesResourceFor(Biomaterial.class)
@Getter
public class BiomaterialController {

  private final @NonNull ProcessRepository processRepository;

  private final @NonNull BiomaterialService biomaterialService;

  private final @NonNull BiomaterialRepository biomaterialRepository;

  private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

  private final @NonNull JsonPatcher jsonPatcher;

  @RequestMapping(path = "submissionEnvelopes/{sub_id}/biomaterials", method = RequestMethod.POST)
  ResponseEntity<Resource<?>> addBiomaterialToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                       @RequestBody Biomaterial biomaterial,
                                                       @RequestParam("updatingUuid") Optional<UUID> updatingUuid,
                                                       PersistentEntityResourceAssembler assembler) {
    updatingUuid.ifPresent(uuid -> {
      biomaterial.setUuid(new Uuid(uuid.toString()));
      biomaterial.setIsUpdate(true);
    });
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

  @RequestMapping(path = "/biomaterials/{id}", method = RequestMethod.PATCH)
  HttpEntity<?> patchBiomaterial(@PathVariable("id") Biomaterial biomaterial,
                                 @RequestBody final ObjectNode patch,
                                 PersistentEntityResourceAssembler assembler) {
    List<String> allowedFields = List.of("content");
    ObjectNode validPatch = patch.retain(allowedFields);
    Biomaterial patchedBiomaterial = jsonPatcher.merge(validPatch, biomaterial);

    Biomaterial entity = biomaterialRepository.save(patchedBiomaterial);
    PersistentEntityResource resource = assembler.toFullResource(entity);
    return  ResponseEntity.accepted().body(resource);
  }
}
