package uk.ac.ebi.subs.ingest.errors.web;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.web.Links;
import uk.ac.ebi.subs.ingest.errors.IngestError;
import uk.ac.ebi.subs.ingest.errors.SubmissionError;
import uk.ac.ebi.subs.ingest.errors.SubmissionErrorService;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@RepositoryRestController
@ExposesResourceFor(SubmissionError.class)
@RequiredArgsConstructor
public class SubmissionErrorController {
  private final @NonNull SubmissionErrorService submissionErrorService;
  private final @NonNull PagedResourcesAssembler pagedResourcesAssembler;

  @DeleteMapping(path = "submissionEnvelopes/{sub_id}" + Links.SUBMISSION_ERRORS_URL)
  public ResponseEntity<SubmissionError> deleteSubmissionEnvelopeErrors(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope) {
    submissionErrorService.deleteSubmissionEnvelopeErrors(submissionEnvelope);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(path = "submissionEnvelopes/{sub_id}" + Links.SUBMISSION_ERRORS_URL)
  public ResponseEntity<PagedResources<Resource<SubmissionError>>> getSubmissionEnvelopeErrors(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      Pageable pageable,
      final PersistentEntityResourceAssembler resourceAssembler) {
    return ResponseEntity.ok(
        pagedResourcesAssembler.toResource(
            submissionErrorService.getErrorsFromEnvelope(submissionEnvelope, pageable),
            resourceAssembler));
  }

  @PostMapping(path = "submissionEnvelopes/{sub_id}" + Links.SUBMISSION_ERRORS_URL)
  public ResponseEntity<PersistentEntityResource> addErrorToEnvelope(
      @PathVariable("sub_id") SubmissionEnvelope submissionEnvelope,
      @RequestBody IngestError ingestError,
      final PersistentEntityResourceAssembler resourceAssembler) {
    SubmissionError submissionError =
        submissionErrorService.addErrorToEnvelope(submissionEnvelope, ingestError);
    PersistentEntityResource submissionErrorResource =
        resourceAssembler.toFullResource(submissionError);
    return ResponseEntity.created(URI.create(submissionErrorResource.getId().getHref()))
        .body(submissionErrorResource);
  }
}
