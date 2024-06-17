package org.humancellatlas.ingest.patch.web;

import org.humancellatlas.ingest.patch.Patch;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PatchResourceProcessor implements ResourceProcessor<Resource<Patch<?>>> {
  private final @NonNull EntityLinks entityLinks;

  @Override
  public Resource<Patch<?>> process(Resource<Patch<?>> resource) {
    Link originalDocumentLink =
        entityLinks
            .linkForSingleResource(resource.getContent().getOriginalDocument())
            .withRel("originalDocument");

    Link updateDocumentLink =
        entityLinks
            .linkForSingleResource(resource.getContent().getUpdateDocument())
            .withRel("updateDocument");

    resource.add(originalDocumentLink);
    resource.add(updateDocumentLink);

    return resource;
  }
}
