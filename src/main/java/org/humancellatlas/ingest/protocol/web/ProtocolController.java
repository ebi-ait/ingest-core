package org.humancellatlas.ingest.protocol.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.protocol.ProtocolService;
import org.humancellatlas.ingest.query.MetadataCriteria;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Javadocs go here!
 *
 * @author Tony Burdett
 * @date 05/09/17
 */
@RepositoryRestController
@ExposesResourceFor(Protocol.class)
@RequiredArgsConstructor
@Getter
public class ProtocolController {
    private final @NonNull ProtocolService protocolService;
    private final @NonNull ProtocolRepository protocolRepository;

    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/protocols", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addProtocolToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                      @RequestBody Protocol protocol,
                                                      @RequestParam("updatingUuid") Optional<UUID> updatingUuid,
                                                      PersistentEntityResourceAssembler assembler) {
        updatingUuid.ifPresent(uuid -> {
            protocol.setUuid(new Uuid(uuid.toString()));
            protocol.setIsUpdate(true);
        });
        Protocol entity = getProtocolService().addProtocolToSubmissionEnvelope(submissionEnvelope, protocol);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/protocols/{protocol_id}", method = RequestMethod.PUT)
    ResponseEntity<Resource<?>> linkProtocolToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                       @PathVariable("id") Protocol protocol,
                                                      PersistentEntityResourceAssembler assembler) {
        Protocol entity = getProtocolService().addProtocolToSubmissionEnvelope(submissionEnvelope, protocol);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }

    //  It's not possible in Angular's HttpClient to send a body, using POST here
    @PostMapping(path = "/protocols/query")
    ResponseEntity<PagedResources<Resource<Protocol>>> queryProtocols(
            @RequestBody List<MetadataCriteria> criteriaList,
            @RequestParam("operator") Optional<String> operator,
            Pageable pageable,
            final PersistentEntityResourceAssembler resourceAssembler) {
        Boolean andCriteria = operator.map("and"::equalsIgnoreCase).orElse(false);
        Page<Protocol> protocols = protocolRepository.findByCriteria(criteriaList, andCriteria, pageable);
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(protocols, resourceAssembler));
    }
}
