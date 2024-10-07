package uk.ac.ebi.subs.ingest.archiving.submission.web;

import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.archiving.submission.ArchiveSubmission;

@Component
@RequiredArgsConstructor
public class ArchiveSubmissionResourceProcessor
    implements ResourceProcessor<Resource<ArchiveSubmission>> {
  private final @NonNull EntityLinks entityLinks;

  private Link getEntitiesLink(ArchiveSubmission archiveSubmission) {
    return entityLinks
        .linkForSingleResource(archiveSubmission)
        .slash("/entities")
        .withRel("entities");
  }

  @Override
  public Resource<ArchiveSubmission> process(Resource<ArchiveSubmission> resource) {
    ArchiveSubmission archiveSubmission = resource.getContent();
    resource.add(getEntitiesLink(archiveSubmission));
    return resource;
  }
}
