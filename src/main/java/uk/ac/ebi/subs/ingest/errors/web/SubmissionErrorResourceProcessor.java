package uk.ac.ebi.subs.ingest.errors.web;

import java.net.URI;

import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.web.Links;
import uk.ac.ebi.subs.ingest.errors.SubmissionError;

@Component
@RequiredArgsConstructor
public class SubmissionErrorResourceProcessor
    implements ResourceProcessor<Resource<SubmissionError>> {
  private final @NonNull EntityLinks entityLinks;

  @Override
  public Resource<SubmissionError> process(Resource<SubmissionError> resource) {
    resource.getContent().setInstance(URI.create(resource.getId().getHref()));
    resource.add(
        entityLinks
            .linkForSingleResource(resource.getContent().getSubmissionEnvelope())
            .slash(Links.SUBMISSION_ERRORS_URL)
            .withRel(Links.SUBMISSION_ERRORS_REL));
    return resource;
  }
}
