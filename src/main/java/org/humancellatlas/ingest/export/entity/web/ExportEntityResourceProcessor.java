package org.humancellatlas.ingest.export.entity.web;

import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.export.entity.ExportEntity;
import org.humancellatlas.ingest.export.job.ExportJob;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExportEntityResourceProcessor implements ResourceProcessor<Resource<ExportEntity>> {
  private final @NonNull EntityLinks entityLinks;

  private Link getSelfLink(ExportEntity exportEntity) {
    return entityLinks
        .linkForSingleResource(exportEntity.getExportJob())
        .slash(Links.EXPORT_JOB_ENTITIES_URL)
        .slash(exportEntity.getId())
        .withSelfRel();
  }

  private Link getExportJobLink(ExportJob exportJob) {
    return entityLinks.linkForSingleResource(exportJob).withRel("exportJob");
  }

  @Override
  public Resource<ExportEntity> process(Resource<ExportEntity> resource) {
    ExportEntity exportEntity = resource.getContent();
    resource.removeLinks();
    resource.add(getSelfLink(exportEntity));
    resource.add(getSelfLink(exportEntity).withRel("exportEntity"));
    resource.add(getExportJobLink(exportEntity.getExportJob()));
    return resource;
  }
}
