package org.humancellatlas.ingest.submissionerror.web;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.humancellatlas.ingest.submission.SubmissionEnvelope;
import org.humancellatlas.ingest.submissionerror.SubmissionError;
import org.humancellatlas.ingest.submissionerror.SubmissionErrorRepository;
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
@Getter
public class SubmissionErrorController {
    private final @NonNull
    SubmissionErrorRepository submissionErrorRepository;

    @RequestMapping(path = "submissionEnvelopes/{sub_id}/submissionErrors", method = RequestMethod.POST)
    ResponseEntity<Resource<?>> addManifestToEnvelope(@PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
                                                      @RequestBody SubmissionError submissionError,
                                                      PersistentEntityResourceAssembler assembler) {
        submissionError.setSubmissionEnvelope(submissionEnvelope);
        SubmissionError error = submissionErrorRepository.save(submissionError);
        PersistentEntityResource resource = assembler.toFullResource(error);
        return ResponseEntity.accepted().body(resource);
    }
}
