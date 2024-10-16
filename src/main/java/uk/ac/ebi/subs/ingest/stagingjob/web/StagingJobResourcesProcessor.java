package uk.ac.ebi.subs.ingest.stagingjob.web;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.Resources;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.ac.ebi.subs.ingest.stagingjob.StagingJob;

@Component
@RequiredArgsConstructor
public class StagingJobResourcesProcessor
    implements ResourceProcessor<Resources<Resource<StagingJob>>> {

  private Link getDeleteByStagingAreaLink() {
    return linkTo(methodOn(StagingJobController.class).deleteStagingJobs(null))
        .withRel("delete-staging-jobs");
  }

  @Override
  public Resources<Resource<StagingJob>> process(Resources<Resource<StagingJob>> resources) {
    resources.add(getDeleteByStagingAreaLink());
    return resources;
  }
}
