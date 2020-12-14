package org.humancellatlas.ingest.protocol.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Uuid;
import org.humancellatlas.ingest.patch.JsonPatcher;
import org.humancellatlas.ingest.project.Project;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolRepository;
import org.humancellatlas.ingest.protocol.ProtocolService;
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

import java.time.Instant;
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

    private final @NonNull JsonPatcher jsonPatcher;

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

    @RequestMapping(path = "/protocols/{id}", method = RequestMethod.PATCH)
    HttpEntity<?> patchProtocol(@PathVariable("id") Protocol protocol,
                                @RequestBody final ObjectNode patch,
                                PersistentEntityResourceAssembler assembler) {
        List<String> allowedFields = List.of("content");
        ObjectNode validPatch = patch.retain(allowedFields);
        Protocol patchedProtocol = jsonPatcher.merge(validPatch, protocol);

        Protocol entity = protocolRepository.save(patchedProtocol);
        PersistentEntityResource resource = assembler.toFullResource(entity);
        return ResponseEntity.accepted().body(resource);
    }
}
