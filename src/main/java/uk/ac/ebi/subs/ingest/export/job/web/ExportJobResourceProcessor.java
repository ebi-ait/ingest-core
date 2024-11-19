package uk.ac.ebi.subs.ingest.export.job.web;

import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.core.web.Links;
import uk.ac.ebi.subs.ingest.export.job.ExportJob;
import uk.ac.ebi.subs.ingest.submission.SubmissionEnvelope;

@Component
@RequiredArgsConstructor
public class ExportJobResourceProcessor implements ResourceProcessor<Resource<ExportJob>> {
  private final @NonNull EntityLinks entityLinks;

  private Link getEntitiesLink(ExportJob exportJob) {
    return entityLinks
        .linkForSingleResource(exportJob)
        .slash(Links.EXPORT_JOB_ENTITIES_URL)
        .withRel(Links.EXPORT_JOB_ENTITIES_REL);
  }

  private Link getEntitiesStatusLink(ExportJob exportJob) {
    return entityLinks
        .linkForSingleResource(exportJob)
        .slash(Links.EXPORT_JOB_ENTITIES_URL + "?status={status}")
        .withRel(Links.EXPORT_JOB_ENTITIES_BY_STATUS_REL);
  }

  private Link getSubmissionLink(SubmissionEnvelope submission) {
    return entityLinks.linkForSingleResource(submission).withRel("submission");
  }

  @Override
  public Resource<ExportJob> process(Resource<ExportJob> resource) {
    ExportJob exportJob = resource.getContent();
    resource.add(getEntitiesLink(exportJob));
    resource.add(getEntitiesStatusLink(exportJob));
    resource.add(getSubmissionLink(exportJob.getSubmission()));
    return resource;
  }
}
