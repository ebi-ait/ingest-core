package org.humancellatlas.ingest.errors.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.errors.SubmissionError;
import org.humancellatlas.ingest.errors.SubmissionErrorService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
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

@RepositoryRestController
@ExposesResourceFor(SubmissionError.class)
@RequiredArgsConstructor
public class SubmissionErrorController {
    private final @NonNull SubmissionErrorService submissionErrorService;

    @RequestMapping(path = "submissionEnvelopes/{sub_id}/submissionErrors", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addErrorToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                   @RequestBody SubmissionError submissionError,
                                                   PersistentEntityResourceAssembler assembler) {

        SubmissionEnvelope envelope = submissionErrorService.addErrorToEnvelope(submissionError, submissionEnvelope);
        PersistentEntityResource resource = assembler.toFullResource(envelope);
        return ResponseEntity.accepted().body(resource);
    }
}
