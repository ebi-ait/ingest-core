package uk.ac.ebi.subs.ingest.submission.web;

import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.*;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.web.Links;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Component
@RequiredArgsConstructor
public class SubmissionEnvelopeCollectionResourceProcessor
    implements ResourceProcessor<RepositoryLinksResource> {
  private final @NonNull EntityLinks entityLinks;

  private Link getUpdateSubmissionsLink() {
    return entityLinks
        .linkFor(SubmissionEnvelope.class)
        .slash(Links.UPDATE_SUBMISSION_URL)
        .withRel(Links.UPDATE_SUBMISSION_REL);
  }

  @Override
  public RepositoryLinksResource process(RepositoryLinksResource resource) {
    resource.add(getUpdateSubmissionsLink());
    return resource;
  }
}
