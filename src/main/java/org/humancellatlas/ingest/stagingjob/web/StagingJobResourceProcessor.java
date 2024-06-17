package org.humancellatlas.ingest.stagingjob.web;

import org.humancellatlas.ingest.core.web.Links;
import org.humancellatlas.ingest.stagingjob.StagingJob;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class StagingJobResourceProcessor implements ResourceProcessor<Resource<StagingJob>> {
  private final @NonNull EntityLinks entityLinks;

  private Link getCompleteStagingJobLink(StagingJob stagingJob) {
    return entityLinks
        .linkForSingleResource(stagingJob)
        .slash(Links.COMPLETE_STAGING_JOB_URL)
        .withRel(Links.COMPLETE_STAGING_JOB_REL);
  }

  @Override
  public Resource<StagingJob> process(Resource<StagingJob> stagingJobResource) {
    StagingJob stagingJob = stagingJobResource.getContent();

    stagingJobResource.add(getCompleteStagingJobLink(stagingJob));

    return stagingJobResource;
  }
}
