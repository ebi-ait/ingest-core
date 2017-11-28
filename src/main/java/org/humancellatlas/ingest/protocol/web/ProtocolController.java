package org.humancellatlas.ingest.protocol.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.Event;
import org.humancellatlas.ingest.state.ValidationState;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.protocol.Protocol;
import org.humancellatlas.ingest.protocol.ProtocolService;
import org.humancellatlas.ingest.state.StateEngine;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
    private final @NonNull StateEngine stateEngine;

    @RequestMapping(path = "/submissionEnvelopes/{sub_id}/protocols", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addProtocolToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                      @RequestBody Protocol protocol,
                                                      PersistentEntityResourceAssembler assembler) {
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

    @RequestMapping(path = "/protocols/{id}" + Links.VALIDATING_URL, method = RequestMethod.PUT)
    HttpEntity<?> validatingProtocol(@PathVariable("id") Protocol protocol) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProtocolService().getProtocolRepository(),
                protocol,
                ValidationState.VALIDATING);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/protocols/{id}" + Links.VALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> validateProtocol(@PathVariable("id") Protocol protocol) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProtocolService().getProtocolRepository(),
                protocol,
                ValidationState.VALID);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/protocols/{id}" + Links.INVALID_URL, method = RequestMethod.PUT)
    HttpEntity<?> invalidateProtocol(@PathVariable("id") Protocol protocol) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProtocolService().getProtocolRepository(),
                protocol,
                ValidationState.INVALID);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/protocols/{id}" + Links.PROCESSING_URL, method = RequestMethod.PUT)
    HttpEntity<?> processingProtocol(@PathVariable("id") Protocol protocol) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProtocolService().getProtocolRepository(),
                protocol,
                ValidationState.PROCESSING);

        return ResponseEntity.accepted().body(event);
    }

    @RequestMapping(path = "/protocols/{id}" + Links.COMPLETE_URL, method = RequestMethod.PUT)
    HttpEntity<?> completeProtocol(@PathVariable("id") Protocol protocol) {
        Event event = this.getStateEngine().advanceStateOfMetadataDocument(
                getProtocolService().getProtocolRepository(),
                protocol,
                ValidationState.COMPLETE);

        return ResponseEntity.accepted().body(event);
    }
}
