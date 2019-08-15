package org.humancellatlas.ingest.errors.web;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.errors.IngestError;
import org.humancellatlas.ingest.errors.SubmissionError;
import org.humancellatlas.ingest.errors.SubmissionErrorRepository;
import org.humancellatlas.ingest.errors.SubmissionErrorService;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@RepositoryRestController
@ExposesResourceFor(SubmissionError.class)
@RequiredArgsConstructor
public class SubmissionErrorController {
    private final @NonNull SubmissionErrorService submissionErrorService;
    private final @NonNull SubmissionErrorRepository submissionErrorRepository;
    private final @NonNull PagedResourcesAssembler<SubmissionError> pagedResourcesAssembler;
    private final @NonNull SubmissionErrorResourceAssembler submissionErrorResourceAssembler;

    @GetMapping(path = Links.SUBMISSION_ERRORS_URL)
    public ResponseEntity<PagedResources<Resource<SubmissionError>>> getSubmissionErrors(Pageable pageable) {
        return ResponseEntity.ok(
                pagedResourcesAssembler.toResource(
                        submissionErrorRepository.findAll(pageable),
                        submissionErrorResourceAssembler
                )
        );
    }

    @GetMapping(path = Links.SUBMISSION_ERRORS_URL + "/{submissionError}")
    public ResponseEntity<Resource<SubmissionError>> getSubmissionError(@PathVariable("submissionError") UUID uuid) {
        return submissionErrorRepository.findById(uuid.toString()).map(
                error -> ResponseEntity.ok(submissionErrorResourceAssembler.toResource(error))
        ).orElse(
                ResponseEntity.notFound().build()
        );
    }

    @GetMapping(path = "submissionEnvelopes/{sub_id}" + Links.SUBMISSION_ERRORS_URL)
    public ResponseEntity<PagedResources<Resource<SubmissionError>>> getSubmissionEnvelopeErrors(
            @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
            Pageable pageable) {
        return ResponseEntity.ok(
                pagedResourcesAssembler.toResource(
                        submissionErrorService.getErrorsFromEnvelope(submissionEnvelope, pageable),
                        submissionErrorResourceAssembler
                )
        );
    }

    @PostMapping(path = "submissionEnvelopes/{sub_id}" + Links.SUBMISSION_ERRORS_URL)
    public ResponseEntity<Resource<SubmissionError>> addErrorToEnvelope(
            @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
            @RequestBody IngestError ingestError) {
        SubmissionError submissionError = submissionErrorService.addErrorToEnvelope(submissionEnvelope, ingestError);
        Resource<SubmissionError> submissionErrorResource = submissionErrorResourceAssembler.toResource(submissionError);
        if (submissionError.getInstance() != null) {
            return ResponseEntity.created(submissionError.getInstance()).body(submissionErrorResource);
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
    }
}
