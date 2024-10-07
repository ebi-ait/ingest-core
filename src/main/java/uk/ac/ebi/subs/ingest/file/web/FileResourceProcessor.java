package uk.ac.ebi.subs.ingest.file.web;

import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.web.Links;
import uk.ac.ebi.subs.ingest.file.File;

@Component
@RequiredArgsConstructor
public class FileResourceProcessor implements ResourceProcessor<Resource<File>> {
  private final @NonNull EntityLinks entityLinks;

  private Link getCreateValidationJobLink(File file) {
    return entityLinks
        .linkForSingleResource(file)
        .slash(Links.FILE_VALIDATION_JOB_URL)
        .withRel(Links.FILE_VALIDATION_JOB_REL);
  }

  @Override
  public Resource<File> process(Resource<File> resource) {
    File fileDocument = resource.getContent();
    resource.add(getCreateValidationJobLink(fileDocument));
    return resource;
  }
}
