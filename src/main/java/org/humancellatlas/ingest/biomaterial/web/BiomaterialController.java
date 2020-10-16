package org.humancellatlas.ingest.biomaterial.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.biomaterial.Biomaterial;
import org.humancellatlas.ingest.biomaterial.BiomaterialService;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.process.Process;
import org.humancellatlas.ingest.process.ProcessRepository;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

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

  @RequestMapping(path = "biomaterials/{id}/inputBiomaterial/{input_id}", method = RequestMethod.PUT)
  ResponseEntity<Resource<?>> addInputBiomaterial(@PathVariable("id") Biomaterial biomaterial,
                                                        @PathVariable("input_id") Biomaterial inputBiomaterial,
                                                        @RequestBody Process process,
                                                        PersistentEntityResourceAssembler assembler) {
    biomaterialService.addInputBiomaterial(inputBiomaterial, process, biomaterial);
    PersistentEntityResource resource = assembler.toFullResource(biomaterial);
    return ResponseEntity.accepted().body(resource);

  }

  @RequestMapping(path = "biomaterials/{id}/inputBiomaterials", method = RequestMethod.GET)
  ResponseEntity<PagedResources> getInputBiomaterials(@PathVariable("id") Biomaterial biomaterial,
                                                   Pageable pageable,
                                                   final PersistentEntityResourceAssembler resourceAssembler) {
    Page<Biomaterial> inputBiomaterials = biomaterialService.getInputBiomaterials(biomaterial, pageable);
    return ResponseEntity.ok(pagedResourcesAssembler.toResource(inputBiomaterials, resourceAssembler));
  }

  @RequestMapping(path = "biomaterials/{id}/inputBiomaterials/{input_id}", method = RequestMethod.DELETE)
  ResponseEntity<Resource<?>> deleteInputBiomaterial(@PathVariable("id") Biomaterial biomaterial,
                                                  @PathVariable("input_id") Biomaterial inputBiomaterial,
                                                  PersistentEntityResourceAssembler assembler) {
    biomaterialService.deleteInputBiomaterial(biomaterial, inputBiomaterial);
    return ResponseEntity.accepted().build();

  }

}
