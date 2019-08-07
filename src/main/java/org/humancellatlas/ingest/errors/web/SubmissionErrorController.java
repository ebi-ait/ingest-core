package org.humancellatlas.ingest.errors.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.errors.*;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RepositoryRestController
@ExposesResourceFor(SubmissionError.class)
@RequiredArgsConstructor
@Getter
public class SubmissionErrorController {
    private final @NonNull SubmissionErrorService submissionErrorService;

    @PostMapping(path = "submissionEnvelopes/{sub_id}/submissionErrors")
    ResponseEntity<Resource<?>> addErrorToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                   @RequestBody SubmissionError submissionError,
                                                   final PersistentEntityResourceAssembler resourceAssembler) {

        SubmissionEnvelope envelope = getSubmissionErrorService().addErrorToEnvelope(submissionError, submissionEnvelope);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(envelope));
    }
}
