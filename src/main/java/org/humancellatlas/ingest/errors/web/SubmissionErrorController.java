package org.humancellatlas.ingest.errors.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.errors.*;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RepositoryRestController
@ExposesResourceFor(SubmissionError.class)
@RequiredArgsConstructor
public class SubmissionErrorController {
    private final @NonNull SubmissionErrorService submissionErrorService;
    private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

    @GetMapping(path = "submissionEnvelopes/{sub_id}/submissionErrors")
    ResponseEntity<?> GetSubmissionErrors(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                          Pageable pageable,
                                          final PersistentEntityResourceAssembler resourceAssembler) {
        return ResponseEntity.ok(pagedResourcesAssembler.toResource(submissionErrorService.getErrorsFromEnvelope(submissionEnvelope,pageable), resourceAssembler));
    }

    @PostMapping(path = "submissionEnvelopes/{sub_id}/submissionErrors")
    ResponseEntity<Resource<?>> addErrorToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                   @RequestBody IngestError ingestError,
                                                   final PersistentEntityResourceAssembler resourceAssembler) {
        SubmissionError submissionError = submissionErrorService.addErrorToEnvelope(submissionEnvelope, ingestError);
        return ResponseEntity.accepted().body(resourceAssembler.toFullResource(submissionError));
    }
}
